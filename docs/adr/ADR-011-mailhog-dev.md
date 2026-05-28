# ADR – 011: MailHog como Servidor SMTP de Desenvolvimento Local

## Data
27/05/2026

---

## Status

- Aceita

---

## Contexto

Com a introdução do módulo de notificações por e-mail (ADR-009), o ambiente de desenvolvimento precisa de uma forma de:

- Validar visualmente o HTML renderizado dos templates Qute antes de subir para staging.
- Confirmar que o fluxo SMTP (`quarkus.mailer.host`, `port`, `from`, autenticação, TLS) está corretamente configurado — não apenas a serialização de objetos.
- Permitir QA manual e demos para stakeholders sem precisar de credenciais de SMTP de provedor real.
- Evitar enviar e-mails reais para endereços de teste durante desenvolvimento.

A configuração anterior usava `quarkus.mailer.mock=true` em `%dev`, que ativa o `MockMailbox` in-memory do Quarkus. Limitações:

- Sem UI — inspeção de e-mails exige escrever testes ou injetar `MockMailbox` em código de debug.
- Sem persistência entre restarts.
- **Pula a stack SMTP inteira** — erros de configuração (host errado, porta errada, TLS mal configurado, from inválido) só aparecem em staging/prod.

Testes automatizados (`%test`) continuam usando `MockMailbox` — é a ferramenta certa para IT in-process; MailHog seria overhead desnecessário em CI.

---

## Decisão

Adotar **MailHog** como servidor SMTP local para o profile `%dev`.

### Setup

- Adicionar serviço `mailhog` ao [docker-compose.yaml](../../software/service-track-api/docker-compose.yaml) — imagem oficial `mailhog/mailhog:latest`, portas `1025` (SMTP) e `8025` (Web UI).
- Atualizar [application-dev.properties](../../software/service-track-api/_infrastructure/src/main/resources/application-dev.properties):
  ```properties
  quarkus.mailer.mock=${QUARKUS_MAILER_MOCK:false}
  quarkus.mailer.host=${QUARKUS_MAILER_HOST:localhost}
  quarkus.mailer.port=${QUARKUS_MAILER_PORT:1025}
  quarkus.mailer.start-tls=DISABLED
  quarkus.mailer.auth-methods=DISABLED
  quarkus.mailer.from=${QUARKUS_MAILER_FROM:no-reply@servicetrack.local}
  ```
- Fallback explícito: dev sem MailHog ativo pode setar `QUARKUS_MAILER_MOCK=true` e voltar ao MockMailbox sem alterar arquivos.
- Container `api` no compose recebe `QUARKUS_MAILER_HOST=mailhog` para usar a rede interna do compose.

### Escopo de uso por profile

| Profile | Mailer backend     | Motivo                                                   |
|---------|--------------------|----------------------------------------------------------|
| `%test` | MockMailbox        | IT in-process, sem rede, rápido em CI                    |
| `%dev`  | MailHog (SMTP)     | Fluxo SMTP real + UI visual para inspeção de templates   |
| `%prod` | SMTP via env vars  | Provedor real (SES, SendGrid, SMTP corporativo)          |

---

## Consequências

### Positivas

- **Inspeção visual** dos e-mails em `http://localhost:8025` — HTML renderizado, headers, cópias, anexos, multipart.
- **Erros de configuração SMTP aparecem em dev**, não em staging — host errado, port fechada, TLS mal configurado falham no commit local.
- **Reproduzível**: outros devs sobem `docker compose up` e têm o mesmo ambiente sem precisar de credenciais.
- **Compatível com QA manual**: stakeholders podem ver e-mails em uma URL sem precisar de caixa de entrada real.
- **Zero código de produção**: configuração 100% em properties e docker-compose; nenhum import novo no app.
- **MockMailbox preservado** em testes automatizados — performance de CI inalterada.

### Negativas

- **+1 container** rodando localmente (~10MB imagem; consumo de memória negligível).
- **Dependência implícita do compose** em dev: rodar o app sem `mailhog` ativo causa erro de conexão. Mitigado pelo fallback `QUARKUS_MAILER_MOCK=true`.
- **MailHog não é mantido ativamente** (último release relevante em 2020). Funciona, mas evolução é nula. Mitigação: substituível por Mailpit (sucessor, mesma interface SMTP) trocando uma linha no compose.
- **Não cobre comportamentos específicos do provedor de produção** (rate limits do SES, throttling do SendGrid, etc.) — apenas valida o protocolo SMTP base.

---

## Alternativas Consideradas

### Opção 1: Manter `quarkus.mailer.mock=true` em dev (status quo)

- Mantém MockMailbox em dev e test.
- Prós: zero infraestrutura; setup mais simples.
- Contras: sem UI visual; pula stack SMTP — erros de configuração só aparecem em staging; QA manual exige código instrumentado.

### Opção 2: Mailpit

- Sucessor moderno do MailHog, ativamente mantido, mesma proposta (SMTP + Web UI).
- Prós: projeto ativo; recursos extras (search, tags, SMTP relay).
- Contras: imagem ligeiramente maior; MailHog ainda funciona bem para o caso de uso simples.

**Justificativa para MailHog sobre Mailpit**: simplicidade e ubiquidade. MailHog é o padrão de facto há anos, documentação abundante, time já familiarizado. Migrar para Mailpit é trivial se aparecer necessidade.

### Opção 3: GreenMail

- Servidor SMTP/IMAP embutido em JVM, útil para testes de integração com IMAP.
- Prós: pode rodar dentro do processo do app (sem container).
- Contras: foco em testes automatizados (redundante com MockMailbox); sem UI; configuração mais complexa para dev manual.

### Opção 4: Conta de teste em provedor real (SES sandbox, Mailtrap.io)

- Usa um SMTP de teste hospedado.
- Prós: comportamento idêntico a produção.
- Contras: requer conta, credenciais; depende de internet; cota limitada; dados de teste vazam para serviço externo.

---

## Referências

- [RFC-011 — MailHog para dev local](../rfc/RFC-011-mailhog-dev.md)
- [ADR-009 — Notificações por e-mail](ADR-009-notificacoes-email.md)
- MailHog — github.com/mailhog/MailHog
- Mailpit (alternativa) — github.com/axllent/mailpit
- Quarkus Mailer Mock — quarkus.io/guides/mailer#using-the-mock-mailer
