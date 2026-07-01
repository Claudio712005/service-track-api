# RFC-012 — Resiliência por Endpoint com MicroProfile Fault Tolerance

## Data
21/06/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para implementação de resiliência em duas camadas (adapter + resource) usando SmallRye Fault Tolerance 6.4.0, com circuit breaker, retry com backoff exponencial, timeout configurável, rate limiting e bulkhead.

---

## Problema

A API ServiceTrack possui 3 integrações externas (FIPE, Unsplash, SMTP) e ~50 endpoints REST com resiliência mínima:

- **FIPE/Unsplash**: Sem retry, circuit breaker ou fallback. Falhas transitórias retornam erro 503 direto ao cliente. Cache (`@CacheResult`) protege apenas chamadas repetidas — cache miss em serviço instável trava thread por até 10s
- **Endpoints públicos**: Login e cadastro sem rate limiting — vulneráveis a brute-force e spam
- **Endpoints pesados**: Dashboard (7+ queries) e listagens sem controle de concorrência — uma rajada de requisições pode saturar o pool de conexões
- **Email**: Única integração com `@Retry` + `@CircuitBreaker`, mas com parâmetros desalinhados do padrão desejado

---

## Proposta Técnica

### Arquitetura de Resiliência em Duas Camadas

```
┌─────────────────────────────────────────────────────────────┐
│ Resource Layer                                              │
│  @Timeout (2-15s por tipo)                                  │
│  @RateLimit (endpoints públicos: 5-20 req/min)              │
│  @Bulkhead (endpoints pesados: 5-15 concurrent)             │
├─────────────────────────────────────────────────────────────┤
│ Application Layer                                           │
│  (sem anotações FT — camada pura de domínio)                │
├─────────────────────────────────────────────────────────────┤
│ Adapter Layer                                               │
│  @Retry(3) + @ExponentialBackoff(factor=2)                  │
│  @CircuitBreaker(threshold=5, ratio=1.0, delay=60s)         │
│  @Timeout(10s)                                              │
│  @Fallback (IntegracaoExternaException ou emptyList)         │
└─────────────────────────────────────────────────────────────┘
```

### Parâmetros do Circuit Breaker

| Parâmetro | Valor | Justificativa |
|-----------|-------|---------------|
| requestVolumeThreshold | 5 | Abre após 5 falhas vistas pelo CB |
| failureRatio | 1.0 | Todas 5 devem ser falhas (consecutivas) |
| delay | 60000ms (1 min) | Tempo em estado OPEN antes de testar half-open |
| successThreshold | 2 | 2 sucessos em half-open para fechar |

### Parâmetros do Retry

| Parâmetro | Valor | Justificativa |
|-----------|-------|---------------|
| maxRetries | 3 | 4 tentativas totais (1 + 3 retries) |
| ExponentialBackoff.factor | 2 | Delays: 1s → 2s → 4s |
| ExponentialBackoff.maxDelay | 10000ms | Teto de 10s entre retries |

### Ordem de Interceptação (MicroProfile FT spec)

```
Fallback( Retry( CircuitBreaker( Timeout( Bulkhead( método ) ) ) ) )
```

- Bulkhead verifica concorrência
- Timeout inicia timer
- CircuitBreaker verifica estado (OPEN → falha rápida)
- Retry retenta em falha
- Fallback captura exceção final

### Exception Mappers

| Exception | HTTP Status | Mensagem |
|-----------|------------|----------|
| TimeoutException | 504 | "Tempo limite excedido" |
| CircuitBreakerOpenException | 503 | "Serviço temporariamente indisponível" |
| BulkheadException | 503 | "Serviço sobrecarregado" |
| RateLimitException | 429 | "Limite de requisições excedido" |

### Interação com @CacheResult

`@CacheResult` (Quarkus Cache) tem prioridade maior que os interceptors FT. Em cache hit, o método não é invocado e FT não atua. Em cache miss, FT protege a chamada ao serviço externo. Comportamento desejado.

---

## Alternativas Consideradas

### Opção 1: Resilience4j

- Biblioteca Java madura com API fluente
- Prós: API programática flexível, métricas Micrometer nativas
- Contras: Requer integração manual com CDI, não faz parte do BOM Quarkus, dependência extra

---

### Opção 2: Configuração apenas via REST Client properties

- Usar `connect-timeout` e `read-timeout` do MicroProfile REST Client
- Prós: Zero código adicional
- Contras: Sem retry, circuit breaker, fallback ou rate limiting. Proteção insuficiente

---

## Pontos em Aberto

- Rate limiting distribuído para deploy multi-pod (Redis) — não necessário na escala atual
- Métricas de FT expostas via MicroProfile Metrics/Micrometer para dashboards de observabilidade

---

## Impactos

### Positivos
- Proteção completa contra falhas de integrações externas com recuperação automática
- Endpoints públicos protegidos contra abuso
- Controle de concorrência em operações pesadas
- Logs diferenciados por camada facilitam diagnóstico
- Configuração externalizável por ambiente

### Negativos
- Latência adicional em cenário de falha (até 7s de backoff antes de fallback)
- Rate limiting per-JVM — limites efetivos multiplicam com número de pods
- Dependência adicional de testes (quarkus-junit5-mockito)

---

## Próximos Passos

- ~~Revisão pelo time~~
- ~~Implementação (ADR-012)~~
- Monitoramento em produção para ajuste de thresholds
- Avaliação de rate limiting distribuído quando necessário
