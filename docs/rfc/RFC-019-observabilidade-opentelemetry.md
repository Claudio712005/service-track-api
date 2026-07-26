# RFC – 019: Observabilidade vendor-neutral com OpenTelemetry

## Data
11/07/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para adoção de **OpenTelemetry** como padrão único de instrumentação, exportando via
**OTLP**, com o backend de observabilidade escolhido **apenas por configuração** — Collector local
em desenvolvimento e Datadog em produção — mantendo a aplicação desacoplada do fornecedor.

---

## Contexto

A aplicação (Quarkus + Kotlin, arquitetura hexagonal) precisa de observabilidade — traces,
métricas e logs — para operar com resiliência e escalabilidade. A instrumentação inicial ficou
acoplada ao Datadog (registry Datadog/StatsD + producer específico do agente), o que impedia rodar
observabilidade local offline e criava lock-in de fornecedor.

---

## Motivação

- Eliminar o acoplamento a um fornecedor específico de observabilidade.
- Permitir desenvolvimento local **totalmente offline**, com traces, métricas e logs visíveis.
- Padronizar a instrumentação em torno de um padrão aberto (OpenTelemetry / OTLP).
- Deixar a arquitetura pronta para usar Datadog em produção sem alterar código.

---

## Objetivos

- Instrumentar a aplicação exclusivamente com OpenTelemetry.
- Exportar traces, métricas e logs via OTLP.
- Selecionar o backend somente por variável de ambiente.
- Prover um ambiente local funcional e offline (Collector + Jaeger + Prometheus + Grafana).
- Concentrar a observabilidade em uma camada dedicada.

---

## Escopo

- Módulo `_infrastructure`: dependências, configuração e camada `observability/`.
- `docker-compose`: profiles `observability` (local) e `datadog` (preparação).
- Configurações do OpenTelemetry Collector, Prometheus e Grafana.
- Documentação: ADR-019, esta RFC e o README.

## Fora do Escopo

- Terraform, Kubernetes, Helm Charts, pipelines de CI/CD e infraestrutura AWS.
- Deploy de produção.
- Criação de dashboards de negócio específicos e alertas.

---

## Arquitetura Atual

```text
Application
    │  Micrometer (registry Datadog / StatsD)
    ▼
Datadog Agent (exige DD_API_KEY)
    │
Datadog Cloud
```

Acoplada ao Datadog; local depende de chave/agent; sem padrão aberto.

---

## Arquitetura Proposta

### Desenvolvimento local (offline)

```text
Application
    │ OpenTelemetry SDK / Micrometer
    │ OTLP
    ▼
OpenTelemetry Collector
    ├── Debug/Logging Exporter  (logs)
    ├── Prometheus Exporter     (métricas)  ─▶ Prometheus ─▶ Grafana
    └── OTLP Exporter → Jaeger  (traces)                 ─▶ Grafana
```

### Produção (preparação, sem alteração de código)

```text
Application
    │ OpenTelemetry SDK / Micrometer
    │ OTLP
    ▼
Datadog Agent
    │
Datadog Cloud
```

ou, opcionalmente, `Application → OTLP → OpenTelemetry Collector → Datadog Agent → Datadog Cloud`.

A troca ocorre apenas alterando `OTEL_EXPORTER_OTLP_ENDPOINT`.

---

## Fluxo de Observabilidade

1. A aplicação gera traces, métricas e logs via OpenTelemetry / Micrometer.
2. Os dados saem via OTLP (gRPC 4317 para traces/logs; HTTP 4318 para métricas).
3. Em local, o Collector recebe e distribui: Jaeger (traces), Prometheus (métricas via
   scrape do exporter), debug (logs).
4. Grafana consome Prometheus e Jaeger para visualização unificada.
5. Em produção, o mesmo OTLP aponta para o Datadog Agent — sem mudança de código.

---

## Estratégia para Desenvolvimento Local

- `docker compose --profile observability up` sobe Collector, Jaeger, Prometheus e Grafana.
- Visualização: Jaeger `:16686` (traces), Prometheus `:9090` (métricas), Grafana `:3001`
  (dashboards), logs no `docker logs servicetrack-otel-collector`.
- Sem Datadog Agent, sem API Key, sem SaaS. A aplicação roda mesmo sem o stack (envios OTLP
  são descartados).

---

## Estratégia para Produção

- Nenhuma alteração de código ou de infraestrutura nesta entrega.
- Quando desejado, apontar `OTEL_EXPORTER_OTLP_ENDPOINT` para o Datadog Agent (OTLP habilitado).
- O profile `datadog` do compose existe apenas como preparação/teste, **não é o padrão**.

---

## Plano de Migração

1. Remover dependências e código específicos do Datadog (registry/StatsD + producer).
2. Adicionar `quarkus-opentelemetry`, `quarkus-micrometer` e `micrometer-registry-otlp`.
3. Criar a camada `observability/` (configuration + metrics).
4. Externalizar endpoints via variáveis OTEL.
5. Adicionar os profiles e configs no `docker-compose`.
6. Validar traces, métricas e logs localmente.
7. (Futuro, fora deste escopo) habilitar o Datadog em produção via configuração.

---

## Riscos

- Quarkus 3.15.1 não possui `quarkus-micrometer-opentelemetry`; métricas usam o registry OTLP do
  Micrometer (equivalente e vendor-neutral).
- Exportação de **logs via OTLP** exige Quarkus 3.16+; em 3.15.1 os logs são vistos pelo stdout da
  aplicação (o pipeline de logs do Collector já fica pronto). Traces e métricas não são afetados.
- Mais componentes no ambiente local (mitigado pelo profile opt-in).
- Possível ruído de logs de conexão OTLP quando o stack local não está ativo (aceitável).

---

## Rollback

- Reverter as dependências e a camada `observability/`, e restaurar a configuração anterior.
- Como a mudança é isolada no módulo `_infrastructure` e no compose (nenhuma infra de produção
  tocada), o rollback é local e de baixo risco.

---

## Próximos Passos

- Revisão pelo time.
- Coleta de feedback.
- (Aprovado) registro em ADR-019.
- Futuro: dashboards e alertas; avaliar upgrade para `quarkus-micrometer-opentelemetry`;
  habilitar Datadog em produção por configuração.
