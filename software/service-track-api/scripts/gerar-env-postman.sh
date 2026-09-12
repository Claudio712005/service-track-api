#!/usr/bin/env bash
set -euo pipefail

AMBIENTE="${1:-hml}"
CONSUMIDOR="${2:-web}"
REGIAO="${AWS_REGION:-us-east-1}"
SAIDA="servicetrack-$AMBIENTE.postman_environment.json"

case "$AMBIENTE" in
  hml|prd) ;;
  *) echo "ambiente invalido: $AMBIENTE (use hml ou prd)" >&2; exit 2 ;;
esac

if ! aws sts get-caller-identity >/dev/null 2>&1; then
  echo "ERRO: credenciais AWS invalidas ou expiradas." >&2
  echo "      renove o laboratorio, ou exporte AWS_PROFILE." >&2
  exit 1
fi

URL="$(aws ssm get-parameter --name "/servicetrack/$AMBIENTE/api/base-url" \
  --region "$REGIAO" --query Parameter.Value --output text 2>/dev/null || true)"
if [ -z "$URL" ]; then
  echo "ERRO: URL da API ausente no SSM. O ambiente $AMBIENTE esta de pe?" >&2
  exit 1
fi

ID_CHAVE="$(aws apigateway get-api-keys --region "$REGIAO" \
  --query "items[?name=='servicetrack-$AMBIENTE-$CONSUMIDOR'].id" --output text 2>/dev/null || true)"
if [ -z "$ID_CHAVE" ]; then
  echo "ERRO: consumidor '$CONSUMIDOR' nao encontrado. Use web, mobile ou ci." >&2
  exit 1
fi
CHAVE="$(aws apigateway get-api-key --api-key "$ID_CHAVE" --include-value \
  --region "$REGIAO" --query value --output text)"

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

echo "$SAIDA gerado"
echo "  url     : $URL"
echo "  chave   : ${CHAVE:0:6}... ($CONSUMIDOR, marcada como secret)"
echo
echo "Importe no Postman: File > Import > $SAIDA"
echo "Selecione o environment no canto superior direito, senao o gateway devolve 403."
echo "O arquivo carrega a chave e e ignorado por git. Nao versione."
