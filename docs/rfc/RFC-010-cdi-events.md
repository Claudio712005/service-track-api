# RFC – 010: CDI Events com Semântica AFTER_SUCCESS para Integração Intra-Monolito

## Data
27/05/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para adoção de CDI Events (`jakarta.enterprise.event.Event` + `@Observes(during = AFTER_SUCCESS)`) como mecanismo de integração desacoplada entre o módulo de Ordem de Serviço e o módulo de Notificação, oferecendo semântica de Outbox sem tabela outbox explícita e sem infraestrutura de mensageria.

---

## Problema

Após a aprovação da [RFC-009](RFC-009-notificacoes-email.md), restam decisões de **integração entre módulos**:

- O módulo de Ordem de Serviço precisa avisar Notificação sobre 7 transições de status (`EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO`, `EM_EXECUCAO`, `CANCELADA` em dois cenários, `FINALIZADA`, `ENTREGUE`).
- Acoplamento direto (OS chama `EnfileirarNotificacaoUseCase`) viola a independência modular (ADR-001) e produz 7 pontos de duplicação.
- Atomicidade lógica é mandatória: notificação só pode existir se a transação de OS commitou; falha de OS não pode gerar notificação órfã.
- Sem broker no MVP (RFC-009).
- Adicionar futuro observer (auditoria adicional, métrica, webhook) não deve exigir alterar todos os 7 use cases de OS.

---

## Proposta Técnica

### Evento de aplicação

`_application/notificacao/event/OrdemServicoStatusAlteradoEvent.kt`:

```kotlin
data class OrdemServicoStatusAlteradoEvent(
    val ordemServicoId: OrdemServicoId,
    val clienteId: UsuarioId,
    val novoStatus: StatusOrdemServicoEnum,
)
```

Imutável, dados mínimos, sem referência a `Notificacao`.

### Listener

`_application/notificacao/event/OrdemServicoStatusAlteradoListener.kt`:

```kotlin
@ApplicationScoped
open class OrdemServicoStatusAlteradoListener(
    private val enfileirar: EnfileirarNotificacaoUseCase,
    private val usuarioRepository: UsuarioRepositoryPort,
) {
    open fun aoAlterarStatus(
        @Observes(during = TransactionPhase.AFTER_SUCCESS) evento: OrdemServicoStatusAlteradoEvent,
    ) {
        val nomeCliente = usuarioRepository.buscarPorId(evento.clienteId)
            ?.obterDados()?.nome ?: return

        val variaveis = VariaveisTemplate.de(mapOf(
            "os" to evento.ordemServicoId.valor,
            "novoStatus" to evento.novoStatus.descricao,
            "nomeCliente" to nomeCliente,
        ))

        enfileirar.executar(
            EnfileirarNotificacaoCommand(
                assunto = AssuntoNotificacao("Atualização da sua OS ${evento.ordemServicoId.valor}"),
                titulo = TituloNotificacao(TipoConteudoNotificacao.MUDANCA_STATUS_OS.titulo),
                descricao = DescricaoNotificacao("Status atualizado para ${evento.novoStatus.descricao}"),
                variaveis = variaveis,
                tipoNotificacao = TipoNotificacao.EMAIL,
                tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
                destinatario = evento.clienteId,
            )
        )
    }
}
```

### Publishers

Os 7 use cases de OS injetam `Event<OrdemServicoStatusAlteradoEvent>` no construtor e disparam após `repository.atualizar(os)`:

```kotlin
class EnviarParaDiagnosticoService(
    private val repository: OrdemServicoRepositoryPort,
    private val jwt: JwtPort,
    private val statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
) : EnviarParaDiagnosticoUseCase {

    override fun enviarParaDiagnostico(ordemServicoId: String): ResumoOrdemServicoResDTO {
        // ... regras de negócio ...
        os.iniciarDiagnostico()
        val atualizada = repository.atualizar(os)

        statusAlteradoEvent.fire(
            OrdemServicoStatusAlteradoEvent(
                ordemServicoId = atualizada.id,
                clienteId = atualizada.clienteId,
                novoStatus = atualizada.obterStatus(),
            )
        )

        return ResumoOrdemServicoResDTO.de(atualizada)
    }
}
```

Mesmo padrão em `GerarOrcamento`, `Aprovar`, `Reprovar`, `Cancelar`, `Finalizar`, `Entregar`.

### Configuração CDI

`OrdemServicoServiceConfig` (`_infrastructure/config/service/ordemServico/`) injeta `Event<OrdemServicoStatusAlteradoEvent>` como parâmetro do método `@Produces` e passa para o construtor do service:

```kotlin
@Produces
@ApplicationScoped
fun enviarParaDiagnosticoUseCase(
    repository: OrdemServicoRepositoryPort,
    jwtPort: JwtPort,
    auditoria: RegistrarAuditoriaPort,
    statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
): EnviarParaDiagnosticoUseCase = AuditoriaProxy.envolver(
    EnviarParaDiagnosticoService(repository, jwtPort, statusAlteradoEvent),
    EnviarParaDiagnosticoUseCase::class.java,
    auditoria,
)
```

### Dependência adicional em `_application`

```kotlin
// _application/build.gradle.kts
implementation("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")
```

Apenas API de annotations (`@ApplicationScoped`, `@Observes`, `Event<T>`). Runtime CDI (Quarkus Arc) fica em `_infrastructure`.

### Semântica AFTER_SUCCESS — fluxo

```
1. POST /ordens-servico/{id}/diagnostico
2. Resource (@Transactional) → service.enviarParaDiagnostico(...)
3. service.executar:
   - buscarPorId
   - iniciarDiagnostico (domain)
   - atualizar (JPA flush dentro da TX)
   - event.fire(OrdemServicoStatusAlteradoEvent)   ← evento enfileirado, não despachado
4. Resource retorna, container faz commit da TX
5. CDI invoca o observer (during=AFTER_SUCCESS)
6. Listener resolve nome cliente + enfileira Notificacao em nova TX
7. Worker scheduler processa PENDENTE em próximo tick (~30s)
```

Cenário de erro:

```
3.b service.executar lança DomainException
4.b TX da OS roll-back
5.b CDI descarta evento (NUNCA invoca observer com during=AFTER_SUCCESS)
6.b Nenhuma Notificacao criada — consistência preservada
```

---

## Alternativas Consideradas

### Opção 1: Chamada direta em cada use case

- `EnfileirarNotificacaoUseCase` injetado em cada um dos 7 services; chamada após `atualizar(os)`.
- Prós: leitura linear; sem mágica CDI; sem dependência cdi-api em `_application`.
- Contras: módulo de OS depende de Notificação (viola separação modular); 7 pontos de duplicação; adicionar observer extra exige alterar 7 services.

### Opção 2: Tabela outbox explícita

- Cada use case insere linha em `outbox(evento, dados_json)`; worker lê e processa.
- Prós: garantia de entrega 100% (sobrevive a crash do app entre commit e observer).
- Contras: tabela e worker extras; redundância com `notificacao PENDENTE` que já é, na prática, a outbox.

### Opção 3: Broker (Kafka, RabbitMQ)

- Publicar evento em tópico; consumer separado processa.
- Prós: desacoplamento total; preparado para microsserviços.
- Contras: infra extra; precisa Outbox para garantir publish↔commit; complexidade incompatível com MVP.

### Opção 4: `during = IN_PROGRESS` (default)

- Observer roda na mesma transação do publisher.
- Prós: atomicidade absoluta — a `Notificacao` é criada na mesma TX da OS.
- Contras: latência REST passa a depender da inserção da notificação; falha no listener faz rollback da operação de OS (efeito colateral inaceitável).

### Opção 5: `during = AFTER_COMPLETION`

- Observer roda após o término da TX, mesmo se ela falhou.
- Prós: observer sempre roda.
- Contras: precisa o próprio observer checar se a TX commitou — código defensivo; `AFTER_SUCCESS` faz exatamente esse check no container.

---

## Pontos em Aberto

- Garantia "exactly-once": se o app cair entre commit da TX e execução do observer, a notificação é perdida. Janela é mínima (millisegundos), mas existe. Mitigação futura: outbox explícita.
- Múltiplos observers para o mesmo evento: ordem de execução não é garantida pelo spec CDI. Quando aparecer caso de uso, definir prioridade via `@Priority`.
- Eventos de outros módulos (Veículo, Insumo, etc.) seguirão mesmo padrão? Decidir caso a caso; este RFC cobre apenas OS.

---

## Impactos

### Positivos

- Desacoplamento: módulo de OS não conhece `Notificacao` nem `Enfileirar...`.
- Atomicidade lógica garantida pelo container.
- Extensibilidade: adicionar listener novo (auditoria, métrica) não toca publishers.
- Zero infraestrutura adicional além de annotation API CDI.
- Padrão reproduzível para futuros eventos de domínio.

### Negativos

- `_application` passa a depender de `jakarta.enterprise.cdi-api` (annotation-only).
- Sem persistência de evento — janela mínima de perda em crash entre commit e observer.
- Magic implícita: leitor do código de OS pode não perceber que o `event.fire(...)` dispara notificação. Mitigação: documentação + nome explícito do evento + ADR-010.

---

## Próximos Passos

- Aprovação formal e geração da [ADR-010](../adr/ADR-010-cdi-events-outbox.md).
- Documentar padrão em onboarding para o time.
- Avaliar migração para outbox explícita se janela de perda virar problema mensurável.
