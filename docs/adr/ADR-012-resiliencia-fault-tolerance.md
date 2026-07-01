# ADR-012 — Resiliência por Endpoint com MicroProfile Fault Tolerance

**Status:** Aceita  
**Data:** 2026-06-21  
**Autores:** Equipe ServiceTrack

---

## Contexto

A API ServiceTrack depende de 3 integrações externas (FIPE, Unsplash, SMTP) e expõe ~50 endpoints REST. Até este ponto, apenas o adapter de email possuía `@Retry` + `@CircuitBreaker`. Os adapters FIPE e Unsplash dependiam exclusivamente de cache (`@CacheResult`) e try/catch manual, sem proteção contra falhas transitórias ou cascata. Nenhum endpoint REST tinha proteção de timeout, rate limiting ou controle de concorrência.

Riscos identificados:
- **Cascata de falhas**: FIPE/Unsplash indisponível travava threads do servidor aguardando timeout do HTTP client (10s)
- **Brute-force**: Endpoints públicos (login, cadastro) sem rate limiting
- **Starvation de recursos**: Endpoints pesados (dashboard, listagens) sem controle de concorrência
- **Ausência de circuit breaker**: Falhas repetidas continuavam tentando chamar serviços indisponíveis

---

## Decisão

Adotar **MicroProfile Fault Tolerance** (SmallRye FT 6.4.0, já presente no projeto) em duas camadas:

### Camada de Adapter (integrações externas)
- **@Retry(maxRetries = 3)** com **@ExponentialBackoff(factor = 2)** — backoff exponencial: 1s → 2s → 4s
- **@CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 1.0, delay = 60000)** — abre após 5 falhas consecutivas, fecha após 1 minuto
- **@Timeout(10000)** — 10s por chamada ao serviço externo
- **@Fallback** — FIPE relança `IntegracaoExternaException` com mensagem limpa; Unsplash retorna `emptyList()`

### Camada de Resource (endpoints REST)
- **@Timeout** por categoria: 2s (leitura por ID), 3s (escrita local), 5s (listagens/transições OS), 10s (dashboard), 12-15s (endpoints com chamada externa)
- **@RateLimit** em endpoints públicos: login (20/min), reset senha (5/min), cadastro (10/min)
- **@Bulkhead** em endpoints pesados: dashboard (10), criarVeiculo (10), sugestões de imagem (5), listar OS (15)

### Exception Mappers
- `TimeoutException` → 504 Gateway Timeout
- `CircuitBreakerOpenException` → 503 Service Unavailable
- `BulkheadException` → 503 Service Unavailable
- `RateLimitException` → 429 Too Many Requests

### Diferenciação de erros
- **Adapter timeout/falha** → `IntegracaoExternaException` via `@Fallback` → 503 com log `[FIPE]/[Unsplash]/[Email]`
- **Resource timeout** → `TimeoutException` → 504 com log na camada de recurso

### Configuração
Todos os valores são externalizáveis via `application.properties` (padrão MicroProfile FT config), sem necessidade de recompilação. FT desabilitado em testes via `MP_Fault_Tolerance_NonFallback_Enabled=false`.

---

## Consequências

### Positivas
- Proteção contra cascata de falhas via circuit breaker com abertura em 5 falhas e recuperação automática em 1 minuto
- Backoff exponencial reduz pressão em serviços instáveis
- Rate limiting protege endpoints públicos contra brute-force
- Bulkhead previne starvation por endpoints pesados
- Logs diferenciados identificam exatamente onde a falha ocorreu (adapter vs resource)
- Valores configuráveis por ambiente sem redeployment

### Negativas
- Latência adicional: retry com backoff pode levar até 7s extras (1s + 2s + 4s) antes de falhar
- Rate limiting é per-JVM — em deploy multi-pod, limite efetivo = valor × pods
- Dependência de testes em `quarkus-junit5-mockito` (Mockito) além do MockK já usado

---

## Alternativas Consideradas

### Resilience4j
- Biblioteca mais rica, mas requer integração manual com CDI/Quarkus
- SmallRye FT já está no BOM do Quarkus, sem dependência extra
- MicroProfile FT tem spec padronizada e config via properties

### Rate limiting distribuído (Redis)
- Necessário para escala horizontal real
- Overhead de infraestrutura não justificado para escala atual
- Pode ser adicionado futuramente sem mudança na API
