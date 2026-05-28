# ADR – 009: Notificações por E-mail — Arquitetura Assíncrona com Worker e Retry

## Data
27/05/2026

---

## Status

- Aceita

---

## Contexto

O sistema precisa notificar clientes (e potencialmente mecânicos) sobre eventos de domínio relevantes — em particular, mudanças de status de Ordem de Serviço (`EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO`, `EM_EXECUCAO`, `FINALIZADA`, `ENTREGUE`, `CANCELADA`).

Restrições e premissas:

- Envio de e-mail é uma operação de I/O com **latência alta e variável** (SMTP, TLS, possíveis filas no provedor) e **taxa de falha não-desprezível** (timeouts, indisponibilidade, soft bounces).
- Não deve impactar a latência percebida pelo usuário final que disparou a mudança de status — a resposta HTTP do REST não pode esperar pelo envio.
- Uma falha de SMTP **não pode** reverter a transação de negócio (mudança de status da OS). Inversamente, se a transação de negócio falhar, **não** deve haver e-mail enviado.
- O sistema precisa suportar múltiplos tipos de conteúdo (templates) e novos tipos surgirão sem necessidade de novas tabelas ou novo código de domínio.
- MVP roda em monolito modular (ADR-001); broker externo (Kafka/RabbitMQ) é overkill para o volume atual.
- Arquitetura hexagonal (ports/adapters) é obrigatória — domínio e application layer não podem conhecer detalhes de infraestrutura (Mailer, Scheduler, JPA).

---

## Decisão

Adotar arquitetura **assíncrona em duas etapas** com persistência intermediária da notificação (Outbox-like) e worker scheduler para envio:

1. **Enfileiramento síncrono**: caso de uso `EnfileirarNotificacaoUseCase` cria uma `Notificacao` no banco com status `PENDENTE`, dentro da mesma transação da operação de domínio que a originou.
2. **Worker scheduler**: `@Scheduled(every="30s")` em `_infrastructure` invoca `ProcessarNotificacoesPendentesUseCase`, que:
   - Lê um lote (`limite=50`) de pendentes via `... FOR UPDATE SKIP LOCKED`
   - Para cada item, **em transação própria** (`REQUIRES_NEW` via `TransactionRunnerPort`):
     - Resolve destinatário (e cópias) → `EmailDestinatarioResolverPort`
     - Renderiza template Qute → `TemplateRendererPort`
     - Envia via SMTP → `EmailGatewayPort` (`Mailer` + `@Retry` + `@CircuitBreaker`)
     - Em sucesso: `marcarComoEnviada()` + `atualizar`
     - Em falha: `registrarTentativaFalha(erro, maxTentativas=3)` — abaixo do limite mantém `PENDENTE` (reprocessada no próximo tick); ao atingir, transita para `FALHA_ENVIO`
3. **Templates Qute** em `resources/templates/notificacao/<TIPO_CONTEUDO>/{subject.txt, body.html, body.txt}`. Adicionar novo tipo = criar pasta; teste paramétrico garante cobertura por valor do enum `TipoConteudoNotificacao`.
4. **Hexagonal**: 4 ports out (`NotificacaoRepositoryPort`, `TemplateRendererPort`, `EmailGatewayPort`, `EmailDestinatarioResolverPort`) + 3 use cases in. Todos os adapters concentrados em `_infrastructure/notificacao/`.

---

## Consequências

### Positivas

- **Latência do REST inalterada**: o usuário recebe resposta tão logo a transação de domínio commita; envio acontece em background.
- **Resiliência a falhas SMTP**: falha de envio não derruba transação de negócio; retry automático até `maxTentativas`.
- **Auditoria completa**: cada notificação fica persistida com `tentativasEnvio`, `ultimoErro`, `dataEnvio`, `statusEnvio`, permitindo investigação post-mortem.
- **Idempotência por design**: o worker pode ser reiniciado sem perder pendências; `SKIP LOCKED` evita processamento duplicado entre réplicas.
- **Extensibilidade**: novos tipos de conteúdo não exigem alterações no domínio nem no worker — apenas templates Qute novos.
- **Testabilidade**: ports permitem mocks em testes unitários; `MockMailbox` em IT; teste paramétrico garante consistência enum↔templates.
- **Aderência ao hexagonal**: domain e application layer ficam livres de imports de `quarkus.mailer.*`, `jakarta.transaction.*` (exceto annotation em adapter), `Mailer`, etc.

### Negativas

- **Latência de entrega**: até 30s entre commit da transação e envio (próximo tick do scheduler). Aceitável para mudança de status; não serve para casos críticos (e.g., 2FA).
- **Schema regenerado em dev/prod (`drop-and-create`)**: notificações antigas são perdidas a cada deploy. Fora do escopo deste plano; tratar com Flyway antes de produção real.
- **Scheduler roda em todas as réplicas**: por ora cobre-se com `concurrentExecution=SKIP` + `SKIP LOCKED`; escala vertical até precisar de Quartz clusterizado.
- **Falha do worker silencia notificações**: se o scheduler parar de executar (bug, deploy travado), pendentes se acumulam sem alerta. Mitigação futura: métrica de "idade da pendente mais antiga".
- **Sem outbox table explícita**: a tabela `notificacao` cumpre esse papel; é simples mas acopla notificação a estado relacional. Migração futura para CDC ou broker exige redesenho.

---

## Alternativas Consideradas

### Opção 1: Envio síncrono dentro da transação de domínio

- O caso de uso de OS chama `Mailer.send(...)` antes de retornar.
- Prós: implementação trivial, latência de entrega ~0.
- Contras: latência do REST atrelada ao SMTP (centenas de ms a vários segundos); falha de SMTP reverte transação de negócio (inaceitável); sem retry; sem auditoria de envio.

### Opção 2: Broker externo (Kafka / RabbitMQ / SQS)

- OS publica evento; consumer separado renderiza e envia.
- Prós: desacoplamento total; preparado para microsserviços; throughput alto.
- Contras: infraestrutura extra (broker, monitoramento, ops); overkill para volume MVP; precisa Outbox transacional para garantir consistência publish↔commit; complexidade operacional alta.

### Opção 3: Async I/O sem persistência (`CompletableFuture` / `Mutiny.Uni`)

- Disparo em thread separada após commit; sem fila.
- Prós: latência baixa; sem tabela intermediária.
- Contras: notificação perdida se app cair entre commit e envio; sem retry; sem auditoria; sem garantia de entrega.

---

## Referências

- [RFC-009 — Notificações por E-mail](../rfc/RFC-009-notificacoes-email.md)
- [ADR-010 — CDI Events com semântica de Outbox](ADR-010-cdi-events-outbox.md)
- [ADR-011 — MailHog para dev local](ADR-011-mailhog-dev.md)
- [Plano de implementação — Notificações por E-mail](../../plan-notificacaoEmail.prompt.md)
- Pattern: [Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html)
- Quarkus Mailer Guide — quarkus.io/guides/mailer
- Quarkus Scheduler Guide — quarkus.io/guides/scheduler-reference
- Qute Templating Engine — quarkus.io/guides/qute
- SmallRye Fault Tolerance — smallrye.io/smallrye-fault-tolerance
