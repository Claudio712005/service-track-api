# ADR – 019: Observabilidade vendor-neutral com OpenTelemetry

## Data
11/07/2026

---

## Status

- Aceita

---

## Contexto

A primeira tentativa de observabilidade acoplou a aplicação diretamente ao Datadog:

- dependência `quarkus-micrometer-registry-datadog` e, depois, `micrometer-registry-statsd`
  configurado no *flavor* DogStatsD;
- um producer CDI (`DatadogStatsdConfig`) específico do transporte do agente Datadog;
- o `docker-compose` exigia o **Datadog Agent** — que **não inicia sem `DD_API_KEY`**
  (`01-check-apikey.sh`), tornando impossível rodar observabilidade local totalmente offline.

Problemas desse acoplamento:

- **Lock-in de fornecedor:** trocar de backend exigiria alterar código e dependências.
- **Ambiente local dependente de SaaS:** sem conta/chave Datadog não havia como visualizar
  traces, métricas e logs localmente.
- **Instrumentação proprietária** onde já existe um padrão aberto e amplamente suportado
  (OpenTelemetry).

Restrições desta mudança: **não** alterar Terraform, Kubernetes, Helm, pipelines, infraestrutura
AWS nem o deploy de produção. O foco é o ambiente de desenvolvimento local e a preparação
arquitetural.

---

## Decisão

Adotar **OpenTelemetry (OTel)** como padrão único de instrumentação, com **OTLP** como protocolo
de saída, e manter o Datadog apenas como **backend de produção selecionável por configuração**.

- **Instrumentação:** extensão `quarkus-opentelemetry` (traces e logs) + `quarkus-micrometer`
  com `micrometer-registry-otlp` (métricas) — tudo exportado via OTLP. Sem dependência
  proprietária de fornecedor.
- **Camada dedicada** `infrastructure/observability/`:
  - `configuration/ObservabilityResourceConfig` — producer CDI do `Resource` OTel
    (`service.name`, `service.namespace`, `deployment.environment`), vendor-neutral;
  - `metrics/OtlpMeterRegistryConfig` — producer do registry OTLP das métricas;
  - `metrics/CommonMetricsTagsConfig` — `MeterFilter` com tags comuns.
- **Configuração externa:** o endpoint é definido por `OTEL_EXPORTER_OTLP_ENDPOINT`
  (e `OTEL_EXPORTER_OTLP_METRICS_ENDPOINT`). A aplicação **não sabe** qual backend recebe os
  dados.
  - Desenvolvimento local: `http://otel-collector:4317`.
  - Produção (futura): `http://datadog-agent:4317` — sem qualquer alteração de código.
- **Ambiente local (offline):** `docker-compose` com profile `observability` sobe
  OpenTelemetry Collector + Jaeger (traces) + Prometheus (métricas) + Grafana (dashboards).
  Nenhuma dependência de Datadog Agent, API Key ou SaaS.
- **Logs:** a exportação de logs via OTLP exige Quarkus 3.16+ (o projeto está em 3.15.1). Por ora
  os logs são consultados no stdout da aplicação; o pipeline de logs do Collector já está pronto
  para quando a plataforma for atualizada. Traces e métricas fluem por OTLP normalmente.
- **Produção (preparação):** profile `datadog` no compose, **não-default**, apenas para deixar
  a estrutura pronta. A infraestrutura real de produção **não é alterada** nesta decisão.

### Explícito

- **Desenvolvimento local utilizará OpenTelemetry** (Collector + Jaeger + Prometheus + Grafana).
- **Produção utilizará Datadog** como backend OTLP.
- **A aplicação permanece desacoplada do fornecedor** — a troca é apenas por configuração.

---

## Consequências

### Positivas
- **Vendor-neutral:** trocar de backend (Datadog, Grafana Cloud, New Relic, etc.) é só mudar o
  endpoint OTLP.
- **Local 100% offline:** traces, métricas e logs visíveis sem conta ou chave de SaaS.
- **Padrão aberto:** OTel é o padrão de mercado para instrumentação; menos código proprietário.
- **Separação de responsabilidades:** toda a observabilidade concentrada em uma camada dedicada.
- **Preparado para produção:** basta apontar o endpoint OTLP para o agente Datadog.

### Negativas
- **Mais componentes locais:** Collector, Jaeger, Prometheus e Grafana no compose (mitigado por
  ficarem sob profile opt-in).
- **Métricas via registry OTLP do Micrometer:** no Quarkus 3.15.1 a extensão
  `quarkus-micrometer-opentelemetry` (que unificaria tudo no SDK OTel) ainda não existe; o
  registry OTLP do Micrometer cobre métricas de forma equivalente e igualmente vendor-neutral.
- **Overhead operacional** de manter configs do Collector/Prometheus/Grafana versionadas.

---

## Alternativas Consideradas

### Opção 1: Manter Micrometer + registry Datadog/StatsD (status quo)
- Métricas direto para o Datadog (SaaS ou agente).
- Prós: menor número de componentes.
- Contras: acoplamento ao fornecedor; local exige chave/agent Datadog; sem padrão aberto.

### Opção 2: OpenTelemetry com backend selecionável por configuração (escolhida)
- Instrumentação OTel + OTLP; backend definido por variável de ambiente.
- Prós: vendor-neutral; local offline; produção via Datadog sem mudar código.
- Contras: mais componentes locais; métricas via registry OTLP do Micrometer no Quarkus 3.15.

### Opção 3: Aguardar `quarkus-micrometer-opentelemetry`
- Extensão que unifica Micrometer e OTel no SDK.
- Prós: pipeline único.
- Contras: indisponível no Quarkus 3.15.1 (exigiria upgrade de plataforma, fora de escopo).
