# ADR – 021: Datadog como backend único de observabilidade

## Data
18/08/2026

---

## Status

- Aceita

Origem: `GLOBAL-RFC-007`. Complementa `ADR-019`, que continua valendo.

---

## Contexto

`ADR-019` decidiu instrumentar com OpenTelemetry e exportar por OTLP, deixando o backend
como decisão de ambiente. A aplicação não conhece o fornecedor: exporta para o nome de rede
`coletor-otlp` e quem responde ali varia.

Na prática, dois backends foram implementados:

| Ambiente | Backend |
|---|---|
| local, profile `grafana` | Collector, Jaeger, Prometheus, Loki, Promtail, Grafana |
| local, profile `datadog` | Agente Datadog |
| `hml`, `prd` | Agente Datadog, via Helm no cluster |

O caminho Grafana era o padrão local. Rodava offline, sem conta e sem chave — vantagem real
para desenvolvimento.

O custo apareceu com o tempo:

- **Dois conjuntos de painéis.** O dashboard do Grafana era provisionado por JSON aqui; os do
  Datadog, por Terraform no repositório de IaC. Toda métrica nova precisava ser desenhada
  duas vezes, e na prática não era — os dois divergiram.
- **Nomes diferentes para a mesma métrica.** O collector achata o nome para o Prometheus:
  `servicetrack.usecase.duracao` vira `servicetrack_usecase_duracao_milliseconds_*`. Consulta
  escrita local não servia na nuvem, e vice-versa.
- **Seis serviços a mais no compose**, mantidos para um caminho que não é o de produção.
- **O ambiente local deixou de ser evidência.** Validar um painel local não dizia nada sobre
  o painel da entrega.

O profile também virou armadilha recorrente: `docker compose up` sem `--profile` subia a
aplicação sem coletor, e ela repetia `UnknownHostException: coletor-otlp` indefinidamente.

---

## Decisão

**O Datadog passa a ser o único backend de observabilidade, do local à produção.**

Saem do compose e do repositório: OpenTelemetry Collector, Jaeger, Prometheus, Loki, Promtail
e Grafana, com os arquivos de configuração e o dashboard provisionado.

Com um caminho só, o `--profile` deixa de ter função. `docker compose up` sobe tudo.

**O que não muda:**

- A instrumentação continua OpenTelemetry puro, exportando por OTLP. `ADR-019` segue válida.
- O alias de rede `coletor-otlp` é mantido. A aplicação continua sem citar o fornecedor em
  nenhuma variável, e trocar de backend continua sendo mudança de ambiente, não de código.
- Logs continuam saindo em JSON no stdout, lidos pelo agente — mesmo mecanismo local e no
  cluster.

---

## Consequências

### Positivas

- Um caminho só. O que se valida local é o que se apresenta na entrega.
- Nomes de métrica idênticos em todo ambiente.
- Seis serviços a menos no compose local.
- Fim da armadilha do profile ausente.

### Negativas

- **Não há mais observabilidade local offline.** Rodar sem `DD_API_KEY` significa rodar sem
  telemetria. A aplicação sobe e funciona; só não há dado.
- Dependência de SaaS de terceiro para uma atividade de desenvolvimento.
- A chave da conta passa a ser insumo de trabalho local, com o cuidado de segredo que isso
  implica — `.env` já é ignorado por git.

### Neutras

- Reverter é barato justamente porque `ADR-019` foi respeitada: a aplicação exporta OTLP
  padrão. Repor um collector no alias `coletor-otlp` traz o caminho antigo de volta sem
  tocar em código de aplicação.
