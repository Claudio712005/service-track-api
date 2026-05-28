# RFC – 011: MailHog como Servidor SMTP de Desenvolvimento Local

## Data
27/05/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para adoção de MailHog (servidor SMTP local com Web UI) no profile `%dev`, substituindo o `quarkus.mailer.mock=true` anterior, para permitir inspeção visual dos e-mails renderizados e validação do fluxo SMTP real durante desenvolvimento, mantendo `MockMailbox` em `%test`.

---

## Problema

Com a introdução do pipeline de notificações por e-mail (RFC-009), o ambiente de desenvolvimento precisa:

- **Inspecionar visualmente** o HTML renderizado dos templates Qute — `MockMailbox` exige código para extrair conteúdo.
- **Validar o fluxo SMTP real** (host, port, from, autenticação, TLS) — `MockMailbox` pula a stack SMTP inteira; erros de configuração só aparecem em staging/prod.
- **QA manual e demos** sem exigir caixa de entrada real ou credenciais de provedor externo.
- **Reproduzibilidade** entre devs sem credenciais compartilhadas.

A configuração anterior em `application-dev.properties`:

```properties
quarkus.mailer.mock=true
```

Limitações observadas durante o desenvolvimento das Fases 3–5:

- Sem UI: para conferir HTML, exigia instrumentar `@QuarkusTest` ou rodar testes ad hoc.
- Sem persistência entre restarts.
- Erros como `start-tls` incorreto ou `from` inválido só apareceriam quando deployado.

---

## Proposta Técnica

### Componentes

| Profile  | Mailer backend     | Motivo                                                         |
|----------|--------------------|----------------------------------------------------------------|
| `%test`  | MockMailbox        | IT in-process; rápido em CI; mantém `QuarkusMailerEmailGatewayAdapterIT` inalterado |
| `%dev`   | MailHog (SMTP)     | Fluxo SMTP real + UI visual em `http://localhost:8025`         |
| `%prod`  | SMTP via env vars  | Provedor real (SES, SendGrid, SMTP corporativo)                |

### Mudança no `docker-compose.yaml`

```yaml
mailhog:
  image: mailhog/mailhog:latest
  container_name: servicetrack-mailhog
  restart: unless-stopped
  networks:
    - servicetrack-network
  ports:
    - "1025:1025"   # SMTP
    - "8025:8025"   # Web UI

api:
  # ...
  depends_on:
    postgres:
      condition: service_healthy
    mailhog:
      condition: service_started
  environment:
    # ...
    QUARKUS_MAILER_HOST: mailhog
    QUARKUS_MAILER_PORT: "1025"
    QUARKUS_MAILER_MOCK: "false"
    QUARKUS_MAILER_START_TLS: DISABLED
    QUARKUS_MAILER_AUTH_METHODS: DISABLED
    QUARKUS_MAILER_FROM: ${QUARKUS_MAILER_FROM:-no-reply@servicetrack.local}
```

### Mudança em `application-dev.properties`

```properties
# Mailer (MailHog local — UI em http://localhost:8025)
# Para fallback in-memory (sem MailHog ativo), defina QUARKUS_MAILER_MOCK=true
quarkus.mailer.mock=${QUARKUS_MAILER_MOCK:false}
quarkus.mailer.host=${QUARKUS_MAILER_HOST:localhost}
quarkus.mailer.port=${QUARKUS_MAILER_PORT:1025}
quarkus.mailer.start-tls=DISABLED
quarkus.mailer.auth-methods=DISABLED
quarkus.mailer.from=${QUARKUS_MAILER_FROM:no-reply@servicetrack.local}
```

### Fluxo de uso

**Dev com `./gradlew quarkusDev` (recomendado):**

```bash
docker compose up -d mailhog postgres
./gradlew :_infrastructure:quarkusDev
# 1. Disparar mudança de status via REST
# 2. Aguardar ~30s (scheduler tick)
# 3. Abrir http://localhost:8025 → ver e-mail renderizado
```

**Dev offline ou rápido (sem MailHog):**

```bash
QUARKUS_MAILER_MOCK=true ./gradlew :_infrastructure:quarkusDev
# Volta ao MockMailbox in-memory
```

**Container completo (`docker compose up`):**

```bash
docker compose up -d
# api conecta a mailhog via rede interna
# UI ainda exposta em http://localhost:8025
```

### Diferenças entre profiles

| Aspecto                  | `%test`           | `%dev`              | `%prod`             |
|--------------------------|-------------------|---------------------|---------------------|
| Backend                  | MockMailbox       | MailHog (`:1025`)   | Provedor SMTP real  |
| Autenticação             | n/a               | DISABLED            | Configurada via env |
| TLS                      | n/a               | DISABLED            | Recomendado STARTTLS|
| Persistência             | RAM (limpa por test) | RAM do container | Provedor            |
| Inspeção                 | `@Inject MockMailbox` | Web UI `:8025`   | Provedor / logs     |

---

## Alternativas Consideradas

### Opção 1: Manter `quarkus.mailer.mock=true` em dev

- Status quo.
- Prós: zero infra adicional.
- Contras: sem UI visual; pula stack SMTP — erros de configuração só aparecem tarde.

### Opção 2: Mailpit

- Sucessor moderno do MailHog, mantido ativamente, mesma proposta (SMTP + Web UI).
- Prós: projeto ativo (último commit recente); recursos extras (search, tags, SMTP relay).
- Contras: imagem ligeiramente maior; MailHog ainda funciona perfeitamente para caso simples.

### Opção 3: GreenMail

- Servidor SMTP/IMAP embutido em JVM.
- Prós: roda dentro do processo do app.
- Contras: foco em testes automatizados (redundante com MockMailbox); sem UI; configuração mais complexa para dev.

### Opção 4: Conta de teste em provedor real

- Mailtrap.io, SES sandbox.
- Prós: comportamento idêntico a produção.
- Contras: requer conta, credenciais; depende de internet; cota limitada; dados de teste vazam para serviço externo.

---

## Pontos em Aberto

- Migração para Mailpit caso MailHog deixe de funcionar com Quarkus 4+ ou tenha CVE não corrigida.
- Eventual integração com testes E2E baseados em browser (Playwright/Cypress) lendo da Web UI do MailHog via API HTTP.

---

## Impactos

### Positivos

- Inspeção visual imediata dos templates renderizados durante desenvolvimento.
- Erros de configuração SMTP (host/port/TLS/from) aparecem em dev, não em staging.
- Reproduzibilidade entre devs (mesma imagem, mesmas portas).
- Compatível com QA manual e demos.
- Zero código de produção alterado.
- MockMailbox preservado em testes — sem regressão de performance em CI.

### Negativos

- +1 container em dev (~10MB).
- Dependência implícita do compose para envio funcionar — mitigado pelo fallback `QUARKUS_MAILER_MOCK=true`.
- MailHog não tem releases recentes (estável, mas evolução pausada); risco baixo de obsolescência.

---

## Próximos Passos

- Aprovação formal e geração da [ADR-011](../adr/ADR-011-mailhog-dev.md).
- Atualizar README com instrução de "rodar `docker compose up -d mailhog` antes de `quarkusDev`".
- Considerar migração para Mailpit em ciclo futuro caso MailHog mostre limitação.
