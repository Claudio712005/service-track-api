# RFC – 009: Notificações por E-mail — Persistência, Worker e Templates Qute

## Data
27/05/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para adoção de pipeline assíncrono de notificações por e-mail com persistência intermediária (`Notificacao PENDENTE`), worker scheduler para envio, templates Qute por tipo de conteúdo, retry automático até 3 tentativas e arquitetura hexagonal estrita (4 ports out, 3 use cases in), no contexto do MVP do ServiceTrack-API.

---

## Problema

Mudanças de status de Ordem de Serviço (`EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO`, `EM_EXECUCAO`, `FINALIZADA`, `ENTREGUE`, `CANCELADA`) precisam disparar e-mails aos clientes. Dores observadas em alternativas exploradas:

- **Envio síncrono dentro da transação de domínio** prende a latência do REST ao SMTP e propaga falhas de e-mail como rollbacks de operações de negócio (inaceitável).
- **Async fire-and-forget** (thread separada, sem persistência) perde notificações em queda do app entre commit e envio; sem retry; sem auditoria.
- **Broker externo** (Kafka, RabbitMQ) introduz infraestrutura desproporcional ao volume MVP e exige Outbox para garantir publish↔commit.
- Sem template engine, geração de HTML por concatenação de strings é frágil, não-testável e fora do controle de design.
- Sem cobertura paramétrica enum↔template, adicionar um `TipoConteudoNotificacao` novo sem template silenciosamente quebra produção.

---

## Proposta Técnica

### Visão geral

```
[Use Case OS]                 [Listener]                 [Worker Scheduler]               [Mailer]
   |                             |                              |                            |
   | event.fire (AFTER_SUCCESS)  |                              |                            |
   |---------------------------->| enfileirar.executar          |                            |
   |                             |----------------------------->| (notificação PENDENTE)     |
   |                             |                              |                            |
   |                             |                  a cada 30s: |                            |
   |                             |                  buscarPendentes(50)                      |
   |                             |                              | para cada (TX nova):       |
   |                             |                              |   resolverEmail            |
   |                             |                              |   renderizar Qute          |
   |                             |                              |   enviar -------------->   |
   |                             |                              |   atualizar status         |
```

### Domain

Entidade `Notificacao` (`_domain`):

- Campos: `id`, `assunto`, `titulo`, `descricao`, `variaveis: VariaveisTemplate`, `tipoNotificacao`, `tipoConteudoNotificacao`, `destinatario`, `copias`, `dataCriacao`, `statusEnvio`, `dataEnvio?`, `visualizada`, `dataVisualizacao?`, `tentativasEnvio`, `ultimoErro?`.
- Comportamentos: `gerar` (factory PENDENTE), `restaurar` (reidratação), `marcarComoEnviada`, `marcarFalhaEnvio`, `registrarTentativaFalha(erro, maxTentativas)`, `visualizar`.
- Invariantes: destinatário não pode estar em cópias; sem duplicatas em cópias; `tentativasEnvio >= 0`; `ENVIADA ⇒ dataEnvio != null`; `visualizada ⇒ dataVisualizacao != null && statusEnvio == ENVIADA`.
- Enums: `StatusEnvio (PENDENTE, ENVIADA, FALHA_ENVIO)`, `TipoNotificacao (EMAIL)`, `TipoConteudoNotificacao (MUDANCA_STATUS_OS, ...)`.
- VOs: `AssuntoNotificacao`, `TituloNotificacao`, `DescricaoNotificacao`, `VariaveisTemplate`, `NotificacaoId`.

### Application (hexagonal)

**Ports out** (`_application/notificacao/ports/out/`):

| Port | Responsabilidade |
|---|---|
| `NotificacaoRepositoryPort` | `salvar`, `buscarPorId`, `buscarPendentesParaEnvio(limite)`, `atualizar` |
| `TemplateRendererPort` | `renderizar(tipoConteudo, variaveis): ConteudoRenderizado` |
| `EmailGatewayPort` | `enviar(EmailMensagem): ResultadoEnvio` |
| `EmailDestinatarioResolverPort` | `resolverEmail(UsuarioId): Email` |

Ports adicionais compartilhados:

| Port | Responsabilidade |
|---|---|
| `TransactionRunnerPort` (`shared/ports/out/`) | `executarEmNovaTransacao(block): T` — abstração para `@Transactional(REQUIRES_NEW)` sem importar `jakarta.transaction` em `_application` |

**Ports in** (`_application/notificacao/ports/in/`):

- `EnfileirarNotificacaoUseCase` — `executar(EnfileirarNotificacaoCommand): NotificacaoId`
- `ProcessarNotificacoesPendentesUseCase` — `executar(): ResultadoLote(totalProcessado, enviadas, falhas)`
- `MarcarNotificacaoVisualizadaUseCase` — `executar(NotificacaoId)`

**Use case implementations** (`_application/notificacao/service/`):

- `EnfileirarNotificacaoUseCaseImpl` — cria `Notificacao.gerar(...)` + `repo.salvar`.
- `ProcessarNotificacoesPendentesUseCaseImpl` — orquestra o lote, delega cada item ao `TransactionRunnerPort`, em sucesso `marcarComoEnviada()`, em falha `registrarTentativaFalha(erro, MAX_TENTATIVAS=3)`.
- `MarcarNotificacaoVisualizadaUseCaseImpl` — `notificacao.visualizar()` + `repo.atualizar`.

### Infrastructure (adapters)

| Componente | Tipo | Detalhes |
|---|---|---|
| `NotificacaoEntity` | Entity Panache | tabela `notificacao` + `notificacao_copias` (tabela auxiliar); `variaveis` serializado como JSON (string); enums `@Enumerated(STRING)` |
| `NotificacaoRepositoryAdapter` | `@ApplicationScoped` | implementa `NotificacaoRepositoryPort`; `buscarPendentesParaEnvio` usa `... ORDER BY data_criacao FOR UPDATE SKIP LOCKED LIMIT :n` |
| `QuteTemplateRendererAdapter` | `@ApplicationScoped` | resolve template por `tipoConteudo.name`; carrega `subject.txt` / `body.html` / `body.txt`; falha rápido com `DomainException` se ausente |
| `QuarkusMailerEmailGatewayAdapter` | `@ApplicationScoped` | injeta `io.quarkus.mailer.Mailer`; `@Retry(maxRetries=2, delay=500)` + `@CircuitBreaker(requestVolumeThreshold=4, failureRatio=0.75, delay=5000)`; converte exceção em `ResultadoEnvio.Falha` |
| `EmailDestinatarioResolverAdapter` | `@ApplicationScoped` | delega para `UsuarioRepositoryPort` |
| `TransactionRunnerAdapter` (`shared/transaction/`) | `@ApplicationScoped` | método `@Transactional(REQUIRES_NEW)` que executa o bloco; bean separado para que CDI proxy seja invocado |
| `EnvioNotificacaoScheduler` | `@ApplicationScoped` | `@Scheduled(every="30s", delayed="15s", concurrentExecution=SKIP, identity="envio-notificacao")`; chama `ProcessarNotificacoesPendentesUseCase.executar()` |
| `NotificacaoServiceConfig` | `@ApplicationScoped` | `@Produces` para os 3 use cases — registro CDI fora do `_application` |

### Templates Qute

Estrutura:

```
_infrastructure/src/main/resources/templates/notificacao/
└── MUDANCA_STATUS_OS/
    ├── subject.txt   → "Sua OS {os} mudou para {novoStatus}"
    ├── body.html     → HTML com {nomeCliente}, {os}, {novoStatus}, {#if observacao}...{/if}
    └── body.txt      → versão texto plano (fallback)
```

Adicionar novo tipo: criar pasta `<NOVO_TIPO>/` com os 3 arquivos. Sem código novo.

`quarkus.qute.strict-rendering=false` em `application.properties` permite variáveis ausentes em renders parciais.

### Configuração

| Profile  | Mailer                              |
|----------|-------------------------------------|
| `%test`  | `quarkus.mailer.mock=true`          |
| `%dev`   | MailHog (host `localhost`, port `1025`) — ver RFC-011 |
| `%prod`  | SMTP real via env vars              |

### Retry e circuit breaker

- `@Retry(maxRetries=2, delay=500)` no `enviar` → até 3 tentativas síncronas dentro do método (SmallRye Fault Tolerance), com 500ms entre cada.
- `@CircuitBreaker` evita marcar 100% das notificações como falha durante apagão de SMTP — abre o circuito após 4 chamadas com 75% de falha; espera 5s antes de half-open.
- Independente: cada notificação tem seu próprio contador `tentativasEnvio` no domínio — limite `MAX_TENTATIVAS=3` é checado pelo use case, independente de quantas vezes o `@Retry` interno disparou.

### Concorrência

- Scheduler com `concurrentExecution=SKIP` impede sobreposição de ticks em uma instância.
- `FOR UPDATE SKIP LOCKED` na query de pendentes permite múltiplas réplicas sem processar a mesma notificação.
- Cada item em `REQUIRES_NEW` — falha em uma notificação não derruba o lote.

### Cobertura template ↔ enum

Teste paramétrico em `QuteTemplateRendererAdapterIT`:

```kotlin
@Test
fun `todos os TipoConteudoNotificacao devem ter templates`() {
    val faltantes = mutableListOf<String>()
    TipoConteudoNotificacao.values().forEach { tipo ->
        try {
            renderer.renderizar(tipo, VariaveisTemplate.VAZIO)
        } catch (e: DomainException) {
            faltantes.add("${tipo.name}: ${e.message}")
        } catch (e: io.quarkus.qute.TemplateException) {
            // variável ausente é OK; template ausente não é
        }
    }
    assertTrue(faltantes.isEmpty(), "Tipos sem template: $faltantes")
}
```

Adicionar valor ao enum sem templates ⇒ CI vermelho.

---

## Alternativas Consideradas

### Opção 1: Envio síncrono dentro do use case de OS

- `Mailer.send(...)` direto no método de mudança de status.
- Prós: trivial; sem worker; sem persistência intermediária.
- Contras: latência REST atrelada a SMTP; falha de SMTP roll-back de transação de negócio; sem retry; sem auditoria.

### Opção 2: Broker externo (Kafka / RabbitMQ)

- Publicar evento; consumer separado renderiza e envia.
- Prós: desacoplamento total; preparado para microsserviços.
- Contras: infra extra; precisa Outbox para garantir publish↔commit; overkill para volume MVP.

### Opção 3: Tabela `outbox` separada de `notificacao`

- `outbox` registra o evento; worker lê outbox e cria `notificacao`; outro worker envia.
- Prós: separação clara entre "evento" e "estado de envio".
- Contras: 2 tabelas + 2 workers; complexidade desnecessária — `notificacao PENDENTE` já cumpre os dois papéis.

---

## Pontos em Aberto

- Migração para Flyway antes de produção — `drop-and-create` perde notificações a cada deploy.
- Métrica de "idade da pendente mais antiga" para detectar scheduler travado.
- Política de purga de notificações antigas (ENVIADAS com >90 dias, FALHA_ENVIO com >30 dias).
- Política formal de retry exponencial em vez de fixo (atual: 3 tentativas síncronas + reprocessamento a cada 30s pelo worker).
- Suporte futuro a `TipoNotificacao.PUSH`, `WHATSAPP`, etc. — atualmente apenas `EMAIL`.

---

## Impactos

### Positivos

- Latência REST inalterada — envio de e-mail desacoplado do response.
- Notificações sobrevivem a falhas de SMTP, restart do app, queda momentânea do provedor.
- Auditoria completa por notificação: `tentativasEnvio`, `ultimoErro`, `dataEnvio`.
- Templates Qute desacoplam design de código; novos tipos exigem apenas pastas com `.html` / `.txt` / `subject.txt`.
- Hexagonal puro: domain e application não conhecem Mailer/Scheduler/JPA.
- Teste paramétrico garante consistência enum↔templates em CI.

### Negativos

- Latência de entrega: até 30s entre commit e envio. Aceitável para mudança de status; inaceitável para 2FA — outro pipeline seria necessário.
- Scheduler em todas as réplicas — escalabilidade limitada sem Quartz clusterizado.
- Sem alerta automático para acúmulo de pendentes — depende de métrica futura.
- `drop-and-create` em dev/prod descarta histórico a cada deploy (problema cross-cutting, não introduzido por este RFC).

---

## Próximos Passos

- Revisão pelo time de backend e arquitetura.
- Aprovação formal e geração da [ADR-009](../adr/ADR-009-notificacoes-email.md) correspondente.
- Implementação completa (Fases 0–6 do plano original).
- Documentação de troubleshooting (logs, dashboards, queries SQL úteis).
- Planejamento de migração para Flyway antes de produção real.
