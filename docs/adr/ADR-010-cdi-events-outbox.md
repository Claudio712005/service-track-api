# ADR – 010: CDI Events com Semântica de Outbox para Integração Intra-Monolito

## Data
27/05/2026

---

## Status

- Aceita

---

## Contexto

O módulo de Ordem de Serviço precisa **avisar** o módulo de Notificação sobre mudanças de status, mas:

- O módulo de OS **não pode** depender diretamente do módulo de Notificação (acoplamento direcional violaria a separação modular do monolito — ADR-001).
- O disparo da notificação **deve** acontecer apenas **se a transação de OS commitou com sucesso** — se a transação falhar (validação tardia, deadlock, etc.), nenhuma notificação deve ser gerada.
- Não há broker de mensageria no MVP (decisão da ADR-009); precisamos de integração intra-processo.
- Já existem 7 use cases de OS que mudam status (`EnviarParaDiagnostico`, `GerarOrcamento`, `AprovarOrcamento`, `ReprovarOrcamento`, `Cancelar`, `Finalizar`, `Entregar`). Replicar `enfileirar.executar(...)` em cada um cria acoplamento direto e duplicação.

---

## Decisão

Adotar **CDI Events** (`jakarta.enterprise.event.Event` + `@Observes`) com `during = TransactionPhase.AFTER_SUCCESS` para integração intra-monolito entre módulo de OS e módulo de Notificação.

### Estrutura

1. **Evento** (`_application/notificacao/event/OrdemServicoStatusAlteradoEvent.kt`) — DTO imutável carregando `ordemServicoId`, `clienteId`, `novoStatus`.
2. **Listener** (`_application/notificacao/event/OrdemServicoStatusAlteradoListener.kt`) — bean `@ApplicationScoped` com método `aoAlterarStatus(@Observes(during = AFTER_SUCCESS) evento)`. Resolve nome do cliente via `UsuarioRepositoryPort` e chama `EnfileirarNotificacaoUseCase`.
3. **Publishers** — os 7 use cases de OS injetam `Event<OrdemServicoStatusAlteradoEvent>` e disparam `event.fire(...)` após `repository.atualizar(os)`.
4. **Dependência CDI** — adicionado `jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1` ao módulo `_application` (apenas annotation API; runtime fica em `_infrastructure` via Quarkus Arc).

### Semântica AFTER_SUCCESS

- Os use cases de OS rodam dentro de transações criadas pelo `@Transactional` aplicado nos resources REST (`_infrastructure/.../OrdemServicoResourceImpl.kt`).
- `event.fire(...)` durante a transação **enfileira** o evento internamente.
- Quando a transação **commita**, o container CDI invoca o observer.
- Se a transação **roll-back**ar (qualquer exceção), o observer **não é invocado** — nenhuma notificação é gerada.
- O observer roda em **nova transação** (necessária porque a TX original já commitou), permitindo que o `EnfileirarNotificacaoUseCase` persista a `Notificacao` em uma transação isolada.

Isso oferece **semântica de Outbox** sem tabela outbox explícita: a `Notificacao` criada pelo listener é, de fato, o próprio registro de outbox, processado posteriormente pelo worker scheduler (ver ADR-009).

---

## Consequências

### Positivas

- **Desacoplamento direcional**: o módulo de OS não conhece `EnfileirarNotificacaoUseCase` nem `Notificacao`. Trocar o mecanismo de notificação (e-mail → push, WhatsApp, etc.) não requer mudanças no módulo de OS.
- **Atomicidade lógica**: `AFTER_SUCCESS` garante que notificação só é gerada se a operação de domínio teve sucesso. Não há janela de inconsistência.
- **Extensibilidade**: novos observers podem assinar o mesmo evento sem alterar publishers (e.g., log de auditoria adicional, métricas, webhook).
- **Zero infraestrutura adicional**: usa apenas CDI (já presente via Quarkus Arc). Sem broker, sem cron externo.
- **Testabilidade**: publishers podem ser testados unitariamente com `Event` mockado (mockk); listener testa-se isoladamente; integração end-to-end funciona via `@QuarkusTest`.

### Negativas

- **CDI no `_application`**: módulo de application passa a depender de `jakarta.enterprise.cdi-api` (annotation-only; sem runtime). Aceito como pragmatismo — CDI é abstração de plataforma Jakarta, não de framework específico.
- **Sem persistência do evento**: se o app cair entre commit da OS e execução do observer, a notificação é perdida. Mitigação: `AFTER_SUCCESS` invoca o observer **dentro** do mesmo JVM antes de devolver controle ao container; janela é mínima (millisegundos). Para garantia mais forte, migrar para Outbox table explícita.
- **Acoplamento de tipo**: evento é uma classe Kotlin em `_application` — se um futuro módulo separado quiser observá-lo, precisará referenciar essa classe. Aceito enquanto monolito modular.
- **Self-invocation pitfall**: CDI events não disparam para handlers no mesmo bean. Não é problema aqui pois listener e publishers vivem em beans distintos.

---

## Alternativas Consideradas

### Opção 1: Chamada direta `enfileirar.executar(...)` em cada use case de OS

- Cada use case de OS injeta `EnfileirarNotificacaoUseCase` e chama explicitamente.
- Prós: trivialmente óbvio em leitura do código; sem mágica CDI.
- Contras: 7 use cases × 1 chamada cada = 7 pontos de duplicação; módulo de OS passa a depender de Notificação (acoplamento direcional); adicionar novo observer (métricas, etc.) requer alterar 7 arquivos.

### Opção 2: Outbox table explícita

- Cada use case de OS insere linha em tabela `outbox`; worker separado lê e dispara notificações.
- Prós: garantia 100% de entrega (mesmo com queda do app entre commit e dispatch); padrão consolidado.
- Contras: tabela extra; lógica de despacho duplicada com a já existente em `ProcessarNotificacoesPendentes`. A própria `Notificacao` PENDENTE já cumpre esse papel — adicionar outbox extra seria redundância.

### Opção 3: Broker de mensageria (Kafka, RabbitMQ)

- Use cases publicam evento em tópico; consumer separado processa.
- Prós: desacoplamento total; preparado para microsserviços.
- Contras: infraestrutura extra; necessidade de Outbox transacional para garantir publish↔commit; complexidade operacional alta — incompatível com MVP em monolito modular (ADR-001).

### Opção 4: `during = IN_PROGRESS` (default)

- Observer roda dentro da mesma transação que publicou o evento.
- Prós: a `Notificacao` PENDENTE entraria na mesma transação da OS — atomicidade absoluta.
- Contras: latência do REST passa a depender da inserção da notificação; uma falha no listener (e.g., template ausente) faria rollback da operação de OS — efeito colateral inaceitável.

---

## Referências

- [RFC-010 — CDI Events para integração intra-monolito](../rfc/RFC-010-cdi-events.md)
- [ADR-009 — Notificações por e-mail](ADR-009-notificacoes-email.md)
- [ADR-001 — Monolito modular](ADR-001-monolito-modular.md)
- Jakarta CDI 4.0 Specification — jakarta.ee/specifications/cdi/4.0
- Pattern: [Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html)
- Quarkus CDI Reference — quarkus.io/guides/cdi-reference
