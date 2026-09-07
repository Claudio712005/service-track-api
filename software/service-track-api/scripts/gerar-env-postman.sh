#!/usr/bin/env bash
set -euo pipefail

AMBIENTE="${1:-hml}"
REGIAO="${AWS_REGION:-us-east-1}"
IAC_DIR="${IAC_DIR:-../../../service-track-aws-iac/iac/environments/$AMBIENTE}"
SAIDA="servicetrack-$AMBIENTE.postman_environment.json"

case "$AMBIENTE" in
  hml|prd) ;;
  *) echo "ambiente invalido: $AMBIENTE (use hml ou prd)" >&2; exit 2 ;;
esac

echo "==> lendo a URL da API no SSM"
URL="$(aws ssm get-parameter --name "/servicetrack/$AMBIENTE/api/base-url" \
  --region "$REGIAO" --query Parameter.Value --output text)"

echo "==> lendo a chave de API no terraform"
if [ ! -d "$IAC_DIR" ]; then
  echo "ERRO: nao encontrei $IAC_DIR" >&2
  echo "      aponte IAC_DIR para iac/environments/$AMBIENTE do repositorio de infraestrutura" >&2
  exit 1
fi
CHAVE="$(terraform -chdir="$IAC_DIR" output -json api_key_values | python3 -c 'import sys,json; print(json.load(sys.stdin)["web"])')"

python3 - "$SAIDA" "$AMBIENTE" "$URL" "$CHAVE" <<'PY'
import json, sys, uuid
saida, ambiente, url, chave = sys.argv[1:5]
env = {
    "id": str(uuid.uuid4()),
    "name": f"ServiceTrack {ambiente}",
    "values": [
        {"key": "baseUrl", "value": url, "type": "default", "enabled": True},
        {"key": "authUrl", "value": url, "type": "default", "enabled": True},
        {"key": "apiKey", "value": chave, "type": "secret", "enabled": True},
    ],
    "_postman_variable_scope": "environment",
}
with open(saida, "w") as f:
    json.dump(env, f, indent=2, ensure_ascii=False)
    f.write("\n")
PY

echo "==> $SAIDA gerado"
echo "    URL   : $URL"
echo "    chave : ${CHAVE:0:6}... (marcada como secret)"
echo
echo "Importe no Postman: File > Import > $SAIDA"
echo "O arquivo contem a chave de API e e ignorado por git. Nao versione."
