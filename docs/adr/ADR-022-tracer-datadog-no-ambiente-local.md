# ADR – 022: Tracer do Datadog no ambiente local, OTLP para métricas

## Data
19/08/2026

---

## Status

- Aceita

Origem: `GLOBAL-RFC-007`. Revê parcialmente `ADR-019` e complementa `ADR-021`.

---

## Contexto

`ADR-021` definiu o Datadog como backend único. A instrumentação seguiu OpenTelemetry, como
manda `ADR-019`. Isso funcionou para métricas, mas deixou três lacunas:

1. **O serviço de autenticação não tinha instrumentação nenhuma.** Instrumentá-lo com
   OpenTelemetry exigiria escrever código no repositório da Lambda — interceptadores,
   configuração de exportador, dependências — para um serviço com uma única rota.
2. **Sem correlação entre log e trace.** O `traceId` estava no MDC, mas o Datadog não ligava
   automaticamente a linha de log ao trace.
3. **AppSec, IAST, SCA e Data Streams são inacessíveis por OTLP.** São recursos do tracer
   proprietário; nenhuma configuração OTel os habilita.

Houve ainda uma tentativa de eliminar o agente e exportar direto para o intake OTLP público
do Datadog. Funcionou — traces retornaram `200`, métricas chegaram — mas foi descartada:
o intake está em Preview, os logs deixariam de ser coletados (exigiria Quarkus 3.16+, e este
repositório está em 3.15.1), e a chave de API passaria a viver dentro do container da
aplicação em vez de só no agente.

---

## Decisão

**Traces pelo tracer do Datadog; métricas por OpenTelemetry.**

| Sinal | Caminho | Porta |
|---|---|---|
| Traces | `dd-java-agent.jar` como `-javaagent` | agente `:8126` |
| Métricas de negócio | Micrometer, exportador OTLP | agente `:4318` |
| Logs | stdout em JSON, lidos pelo socket do Docker | — |

O tracer entra nas imagens da aplicação e do serviço de autenticação, **desligado por
padrão** (`DD_TRACE_ENABLED=false`). Só o compose liga. Sem agente alcançável, a imagem se
comporta como antes.

`quarkus.otel.traces.enabled` passa a ser variável de ambiente, para que os dois
instrumentadores nunca emitam o mesmo span duas vezes. O padrão continua `true`, que é o
comportamento de `hml` e `prd`.

**AppSec, IAST e SCA ficam de fora por ora** — são faturados à parte, e a conta é de
avaliação. As variáveis são de ativação, então entram sem mudança estrutural quando houver
decisão comercial.

---

## Consequências

### Positivas

- O fluxo de autenticação passa a ser visível no APM sem uma linha de código nova. Era o
  ponto cego mais grave da Fase 3, já que é o fluxo que o enunciado pede para demonstrar.
- `DD_LOGS_INJECTION` correlaciona log e trace automaticamente.
- `DD_SERVICE`, `DD_ENV` e `DD_VERSION` nomeiam cada serviço de forma consistente entre
  traces, logs e métricas.
- Instrumentação de JDBC, JAX-RS e cliente HTTP sai de graça.

### Negativas

- **Acoplamento ao fornecedor dentro da imagem.** Era exatamente o que `ADR-019` evitou.
  Trocar de backend agora exige reconstruir imagem, não só mudar variável.
- **Local e nuvem passam a divergir no caminho de traces.** `hml` e `prd` continuam em OTLP;
  local usa o tracer. Isso enfraquece o objetivo declarado em `GLOBAL-RFC-007` de que o local
  seja evidência do que roda em produção. A divergência é consciente e está limitada a um
  sinal — métricas e logs seguem idênticos.
- Nomes de métrica derivadas de trace diferem entre os dois caminhos. Os monitores em
  `iac/modules/observability` consultam `trace.http.server.request`, que é o nome gerado pela
  conversão OTLP. Não usar esses nomes como referência ao olhar o ambiente local.
- A imagem cresce com o jar do tracer.

### Reversão

Trocar `DD_TRACE_ENABLED` para `false` e `OTEL_TRACES_ENABLED` para `true` devolve o
comportamento anterior sem reconstruir nada. O jar fica ocioso na imagem.
