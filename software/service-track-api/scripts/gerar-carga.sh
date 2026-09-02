#!/usr/bin/env bash
set -euo pipefail

API_URL="${API_URL:-http://localhost:8080}"
AUTH_URL="${AUTH_URL:-http://localhost:8081}"
ITERACOES="${ITERACOES:-50}"
PARALELAS="${PARALELAS:-4}"
INTERVALO="${INTERVALO:-0.2}"
TAXA_DE_ERRO="${TAXA_DE_ERRO:-15}"

CPF_CLIENTE="${CPF_CLIENTE:-13646633093}"
CPF_MECANICO="${CPF_MECANICO:-98124421030}"
SENHA="${SENHA:-Senha@123}"

TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT

autenticar() {
  local cpf="$1" resposta codigo
  resposta="$(curl -s -w '\n%{http_code}' -X POST "$AUTH_URL/autenticacao" \
    -H 'Content-Type: application/json' \
    -d "{\"cpf\":\"$cpf\",\"senha\":\"$SENHA\"}")"
  codigo="$(tail -n1 <<<"$resposta")"
  if [ "$codigo" != "200" ]; then
    echo "ERRO: login do CPF $cpf retornou HTTP $codigo" >&2
    echo "      confira se o servico de autenticacao esta no ar em $AUTH_URL" >&2
    return 1
  fi
  sed '$d' <<<"$resposta" | python3 -c 'import sys,json; print(json.load(sys.stdin)["token"])'
}

requisitar() {
  local token="$1" metodo="$2" rota="$3" codigo
  codigo="$(curl -s -o /dev/null -w '%{http_code}' -X "$metodo" "$API_URL$rota" \
    -H "Authorization: Bearer $token" --max-time 15 || echo 000)"
  echo "$codigo $metodo $rota" >> "$TEMP_DIR/resultados"
}

ROTAS_CLIENTE=(
  "GET /veiculos"
  "GET /catalogo/servicos"
  "GET /catalogo/insumos"
  "GET /ordem-servico/lista"
  "GET /notificacoes"
  "GET /notificacoes/nao-lidas/contagem"
)

ROTAS_MECANICO=(
  "GET /servicos"
  "GET /insumos"
  "GET /mecanicos"
  "GET /veiculos"
  "GET /ordem-servico/lista"
)

gerar_erros() {
  local token="$1" sorteio
  sorteio=$((RANDOM % 3))
  case $sorteio in
    0) requisitar "token-invalido-de-proposito" GET "/veiculos" ;;
    1) requisitar "$token" GET "/veiculos/$(uuidgen | tr 'A-Z' 'a-z')" ;;
    2) requisitar "$token" GET "/rota-que-nao-existe" ;;
  esac
}

rodada() {
  local indice="$1" token_cliente="$2" token_mecanico="$3"
  local rota metodo caminho

  if [ $((RANDOM % 100)) -lt "$TAXA_DE_ERRO" ]; then
    gerar_erros "$token_cliente"
    return
  fi

  if [ $((indice % 2)) -eq 0 ]; then
    rota="${ROTAS_CLIENTE[$((RANDOM % ${#ROTAS_CLIENTE[@]}))]}"
    metodo="${rota%% *}"; caminho="${rota#* }"
    requisitar "$token_cliente" "$metodo" "$caminho"
  else
    rota="${ROTAS_MECANICO[$((RANDOM % ${#ROTAS_MECANICO[@]}))]}"
    metodo="${rota%% *}"; caminho="${rota#* }"
    requisitar "$token_mecanico" "$metodo" "$caminho"
  fi
}

echo "==> autenticando"
TOKEN_CLIENTE="$(autenticar "$CPF_CLIENTE")"
TOKEN_MECANICO="$(autenticar "$CPF_MECANICO")"
echo "    cliente e mecanico autenticados"

echo "==> disparando $ITERACOES rodadas, $PARALELAS em paralelo, ~$TAXA_DE_ERRO% de erro proposital"
: > "$TEMP_DIR/resultados"
INICIO="$(date +%s)"

for i in $(seq 1 "$ITERACOES"); do
  rodada "$i" "$TOKEN_CLIENTE" "$TOKEN_MECANICO" &
  if [ $((i % PARALELAS)) -eq 0 ]; then
    wait
    sleep "$INTERVALO"
    printf '\r    %s/%s' "$i" "$ITERACOES"
  fi
done
wait
printf '\r    %s/%s concluidas\n' "$ITERACOES" "$ITERACOES"

DURACAO=$(( $(date +%s) - INICIO ))
[ "$DURACAO" -eq 0 ] && DURACAO=1

echo
echo "==> resumo por status"
sort "$TEMP_DIR/resultados" | awk '{print $1}' | uniq -c | sort -rn | while read -r n codigo; do
  printf '    %-5s %s\n' "$codigo" "$n"
done

echo
echo "==> rotas mais chamadas"
awk '{print $2, $3}' "$TEMP_DIR/resultados" | sort | uniq -c | sort -rn | head -8 | while read -r n resto; do
  printf '    %-4s %s\n' "$n" "$resto"
done

TOTAL="$(wc -l < "$TEMP_DIR/resultados" | tr -d ' ')"
echo
echo "==> $TOTAL requisicoes em ~${DURACAO}s (~$((TOTAL / DURACAO)) req/s)"
echo "    metricas levam ate 15s para exportar e mais um pouco para indexar"
echo "    APM:      https://app.datadoghq.com/apm/services?env=local"
echo "    Metricas: https://app.datadoghq.com/metric/explorer  (servicetrack.usecase.*)"
echo "    Logs:     https://app.datadoghq.com/logs?query=env%3Alocal"
