#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
K8S_DIR="$ROOT_DIR/infra/k8s"
TF_DIR="$ROOT_DIR/infra/terraform"

: "${AWS_PROFILE=aws-student}"
aws_profile_arg=()
if [ -z "$AWS_PROFILE" ]; then
  unset AWS_PROFILE
else
  export AWS_PROFILE
  aws_profile_arg=(--profile "$AWS_PROFILE")
fi

terraform -chdir="$TF_DIR" init -input=false -reconfigure >/dev/null
tf() { terraform -chdir="$TF_DIR" "$@"; }

echo ">> Lendo outputs do Terraform..."
ECR_REPO="$(tf output -raw ecr_repository_url)"
export ECR_IMAGE="${ECR_IMAGE:-${ECR_REPO}:latest}"
export RDS_JDBC_URL="$(tf output -raw rds_jdbc_url)"
export RDS_USERNAME="$(tf output -raw db_username)"
export RDS_PASSWORD="$(tf output -raw db_password)"
CLUSTER_NAME="$(tf output -raw cluster_name)"
REGION="$(tf output -raw region)"

echo ">> Configurando kubeconfig ($CLUSTER_NAME / $REGION)..."
aws eks update-kubeconfig --name "$CLUSTER_NAME" --region "$REGION" "${aws_profile_arg[@]}"

echo ">> Instalando metrics-server (necessário para o HPA)..."
kubectl apply -f "https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml"
kubectl -n kube-system rollout status deploy/metrics-server --timeout=120s

echo ">> Aplicando manifestos..."
kubectl apply -f "$K8S_DIR/namespace.yaml"

echo ">> Provisionando Secret das chaves JWT..."

JWT_TMP="$(mktemp -d)"
trap 'rm -rf "$JWT_TMP"' EXIT
if [ -n "${JWT_PRIVATE_KEY_B64:-}" ] && [ -n "${JWT_PUBLIC_KEY_B64:-}" ]; then
  echo "$JWT_PRIVATE_KEY_B64" | base64 -d > "$JWT_TMP/privateKey.pem"
  echo "$JWT_PUBLIC_KEY_B64"  | base64 -d > "$JWT_TMP/publicKey.pem"
else
  cp "$ROOT_DIR/software/service-track-api/_infrastructure/src/main/resources/keys/privateKey.pem" "$JWT_TMP/"
  cp "$ROOT_DIR/software/service-track-api/_infrastructure/src/main/resources/keys/publicKey.pem"  "$JWT_TMP/"
fi
kubectl -n service-track create secret generic service-track-jwt \
  --from-file=privateKey.pem="$JWT_TMP/privateKey.pem" \
  --from-file=publicKey.pem="$JWT_TMP/publicKey.pem" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl apply -f "$K8S_DIR/mailhog.yaml"
envsubst < "$K8S_DIR/configmap.yaml"  | kubectl apply -f -
envsubst < "$K8S_DIR/secret.yaml"     | kubectl apply -f -
envsubst < "$K8S_DIR/deployment.yaml" | kubectl apply -f -
kubectl apply -f "$K8S_DIR/service.yaml"
kubectl apply -f "$K8S_DIR/hpa.yaml"

echo ">> Aguardando rollout..."
kubectl -n service-track rollout status deploy/service-track-app --timeout=300s

echo ">> Status:"
kubectl -n service-track get pods,svc,hpa
echo ">> OK. HPA deve mostrar TARGETS em %/70% (não <unknown>) após ~1min."