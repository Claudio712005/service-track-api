#!/usr/bin/env bash
set -euo pipefail

AMBIENTE="${1:-hml}"
CONSUMIDOR="${2:-web}"
REGIAO="${AWS_REGION:-us-east-1}"
BASE="service-track.postman_collection.json"
SAIDA="servicetrack-$AMBIENTE.postman_collection.json"

case "$AMBIENTE" in
  hml|prd) ;;
  *) echo "ambiente invalido: $AMBIENTE (use hml ou prd)" >&2; exit 2 ;;
esac
[ -f "$BASE" ] || { echo "ERRO: $BASE nao encontrado. Rode a partir de software/service-track-api." >&2; exit 1; }

if ! aws sts get-caller-identity >/dev/null 2>&1; then
  echo "ERRO: credenciais AWS invalidas ou expiradas." >&2
  exit 1
fi

URL="$(aws ssm get-parameter --name "/servicetrack/$AMBIENTE/api/base-url" \
  --region "$REGIAO" --query Parameter.Value --output text 2>/dev/null || true)"
[ -z "$URL" ] && { echo "ERRO: URL da API ausente no SSM. O ambiente $AMBIENTE esta de pe?" >&2; exit 1; }

ID="$(aws apigateway get-api-keys --region "$REGIAO" \
  --query "items[?name=='servicetrack-$AMBIENTE-$CONSUMIDOR'].id" --output text 2>/dev/null || true)"
[ -z "$ID" ] && { echo "ERRO: consumidor '$CONSUMIDOR' nao encontrado. Use web, mobile ou ci." >&2; exit 1; }
CHAVE="$(aws apigateway get-api-key --api-key "$ID" --include-value --region "$REGIAO" --query value --output text)"

python3 - "$BASE" "$SAIDA" "$AMBIENTE" "$URL" "$CHAVE" <<'PY'
import json, sys

base, saida, ambiente, url, chave = sys.argv[1:6]
with open(base) as f:
    c = json.load(f)

c["info"]["name"] = f"ServiceTrack API — {ambiente}"
c["info"].pop("_postman_id", None)

valores = {"baseUrl": url, "authUrl": url, "apiKey": chave}
existentes = {v["key"] for v in c.get("variable", [])}
for v in c.setdefault("variable", []):
    if v["key"] in valores:
        v["value"] = valores[v["key"]]
for k, val in valores.items():
    if k not in existentes:
        c["variable"].append({"key": k, "value": val, "type": "string"})

with open(saida, "w") as f:
    json.dump(c, f, indent=2, ensure_ascii=False)
    f.write("\n")

print(f"{saida} gerado")
print(f"  colecao : {c['info']['name']}")
print(f"  url     : {url}")
print(f"  chave   : {chave[:6]}... ({len(chave)} chars)")
PY

echo
echo "Importe no Postman: File > Import > $SAIDA"
echo "Nao precisa selecionar environment: url e chave ja estao na colecao."
echo
echo "O arquivo carrega a chave de API e e ignorado por git. Nao versione."
