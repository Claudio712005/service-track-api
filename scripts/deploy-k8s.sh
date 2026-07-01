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

export APP_DB_USER="${APP_DB_USER:-app_user}"
export FLYWAY_DB_USER="${FLYWAY_DB_USER:-flyway_user}"
: "${APP_DB_PASSWORD:?defina APP_DB_PASSWORD (secret) — senha do role app_user}"
: "${FLYWAY_DB_PASSWORD:?defina FLYWAY_DB_PASSWORD (secret) — senha do role flyway_user}"
export APP_DB_PASSWORD FLYWAY_DB_PASSWORD

RDS_HOST="$(echo "$RDS_JDBC_URL" | sed -E 's#^jdbc:postgresql://([^:/]+).*#\1#')"
RDS_PORT="$(echo "$RDS_JDBC_URL" | sed -E 's#^jdbc:postgresql://[^:/]+:([0-9]+).*#\1#')"
RDS_DB="$(echo "$RDS_JDBC_URL" | sed -E 's#.*/([^/?]+)(\?.*)?$#\1#')"

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
  
  printf '%s' "$JWT_PRIVATE_KEY_B64" | tr -d '[:space:]' | base64 -d > "$JWT_TMP/privateKey.pem"
  printf '%s' "$JWT_PUBLIC_KEY_B64"  | tr -d '[:space:]' | base64 -d > "$JWT_TMP/publicKey.pem"
else
  cp "$ROOT_DIR/software/service-track-api/_infrastructure/src/main/resources/keys/privateKey.pem" "$JWT_TMP/"
  cp "$ROOT_DIR/software/service-track-api/_infrastructure/src/main/resources/keys/publicKey.pem"  "$JWT_TMP/"
fi
kubectl -n service-track create secret generic service-track-jwt \
  --from-file=privateKey.pem="$JWT_TMP/privateKey.pem" \
  --from-file=publicKey.pem="$JWT_TMP/publicKey.pem" \
  --dry-run=client -o yaml | kubectl apply -f -

echo ">> Provisionando roles no RDS (flyway_user / app_user)..."

kubectl -n service-track create configmap db-init-script \
  --from-file=01-init-roles.sh="$ROOT_DIR/software/service-track-api/scripts/postgres-init/01-init-roles.sh" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl -n service-track create secret generic db-init-creds \
  --from-literal=PGHOST="$RDS_HOST" \
  --from-literal=PGPORT="${RDS_PORT:-5432}" \
  --from-literal=PGUSER="$RDS_USERNAME" \
  --from-literal=PGPASSWORD="$RDS_PASSWORD" \
  --from-literal=PGDATABASE="${RDS_DB:-servicetrack}" \
  --from-literal=FLYWAY_DB_USER="$FLYWAY_DB_USER" \
  --from-literal=FLYWAY_DB_PASSWORD="$FLYWAY_DB_PASSWORD" \
  --from-literal=APP_DB_USER="$APP_DB_USER" \
  --from-literal=APP_DB_PASSWORD="$APP_DB_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl -n service-track delete job service-track-db-init --ignore-not-found
kubectl apply -f "$K8S_DIR/db-init-job.yaml"
kubectl -n service-track wait --for=condition=complete job/service-track-db-init --timeout=120s

kubectl apply -f "$K8S_DIR/mailhog.yaml"
envsubst < "$K8S_DIR/configmap.yaml"  | kubectl apply -f -
envsubst < "$K8S_DIR/secret.yaml"     | kubectl apply -f -
envsubst < "$K8S_DIR/deployment.yaml" | kubectl apply -f -
kubectl apply -f "$K8S_DIR/service.yaml"
kubectl apply -f "$K8S_DIR/service-lb.yaml"
kubectl apply -f "$K8S_DIR/hpa.yaml"

echo ">> Aguardando rollout..."
kubectl -n service-track rollout status deploy/service-track-app --timeout=300s

echo ">> Aguardando endereço público (ELB provisiona de forma assíncrona)..."
APP_URL=""
for _ in $(seq 1 30); do
  APP_URL="$(kubectl -n service-track get svc service-track-app-lb \
    -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || true)"
  [ -n "$APP_URL" ] && break
  sleep 10
done

echo ">> Status:"
kubectl -n service-track get pods,svc,hpa

if [ -n "$APP_URL" ]; then
  echo ">> App disponível em: http://$APP_URL"
else
  echo ">> ELB ainda provisionando. Rode em ~1min:"
  echo "   kubectl -n service-track get svc service-track-app-lb"
fi
echo ">> OK. HPA deve mostrar TARGETS em %/70% (não <unknown>) após ~1min."