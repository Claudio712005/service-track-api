# Domain Layer — ServiceTrack API

## 1. Responsabilidade da camada

A camada `_domain` é o núcleo do sistema. Ela contém **todo o conhecimento do negócio** da oficina mecânica: regras de negócio, invariantes, ciclo de vida das entidades e a linguagem ubíqua do domínio.

Esta camada é completamente **isolada de infraestrutura**. Não depende de frameworks, bancos de dados, HTTP, filas ou qualquer detalhe técnico externo. Toda regra que pode ser expressa em termos de negócio pertence aqui — se uma regra precisa de uma chamada de banco para ser validada, ela não pertence ao domínio.

A arquitetura segue os princípios de **Domain-Driven Design (DDD)**, com separação clara entre Entidades, Value Objects e Aggregates, e com as regras de transição de estado encapsuladas dentro das próprias entidades.

---

## 2. Modelo de domínio

### Linguagem Ubíqua

| Termo do Negócio | Representação no Código |
|---|---|
| Ordem de Serviço | `OrdemServico` |
| Orçamento | `Orcamento` |
| Insumo / Peça | `Insumo` |
| Mecânico | `Mecanico` |
| Cliente | `Usuario` (com `Role.CLIENTE`) |
| Veículo | `Veiculo` |
| Prazo de conclusão | `PrazoConclusao` |
| Status da OS | `StatusOrdemServicoEnum` |
| Nível do mecânico | `NivelMecanicoEnum` |

---

### Entidades

#### `OrdemServico`
Aggregate root central do sistema. Representa o ciclo de vida completo de um atendimento, desde a abertura até a entrega do veículo. Orquestra as transições de status, a gestão de insumos e a aprovação do orçamento.

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `OrdemServicoId` | Identidade da OS |
| `motivo` | `String` | Razão da abertura da OS |
| `clienteId` | `UsuarioId` | Referência ao cliente |
| `mecanicoId` | `UsuarioId` | Mecânico responsável |
| `veiculoId` | `VeiculoId` | Veículo em atendimento |
| `status` | `StatusOrdemServico` | Estado atual com máquina de estados |
| `orcamento` | `Orcamento?` | Orçamento gerado durante o diagnóstico |
| `insumos` | `List<InsumoId>` | Peças e insumos utilizados |
| `prazoConclusao` | `PrazoConclusao?` | Prazo estimado de entrega |

#### `Usuario`
Representa qualquer pessoa autenticada no sistema. O papel de negócio (cliente ou mecânico) é determinado pelo conjunto de `Role` associado.

#### `Veiculo`
Veículo cadastrado e vinculado a um proprietário. Armazena placa, modelo, marca e ano.

#### `Mecanico`
Perfil técnico do mecânico. Contém nível de experiência (`JUNIOR`, `PLENO`, `SENIOR`) e valor por hora, utilizados para calcular o custo da mão de obra.

#### `Insumo`
Peça ou insumo utilizado nos serviços. Controla estoque disponível, estoque mínimo e custo unitário.

---

### Value Objects

Value Objects são imutáveis, definidos pelos seus atributos e carregam validações internas.

| Value Object | Validação encapsulada |
|---|---|
| `Cpf` | 11 dígitos + verificação dos dois dígitos verificadores |
| `Email` | Formato `local@dominio` via regex |
| `Senha` | Mínimo 6 caracteres, maiúscula, minúscula, número e caractere especial |
| `Telefone` | Apenas dígitos, entre 10 e 11 caracteres |
| `Placa` | Formato Mercosul (`ABC1D23`) ou antigo (`ABC1234`) |
| `ValorMonetario` | Não negativo; expõe `somar()` e `multiplicar()` com arredondamento `HALF_UP` |
| `ValorHora` | Deve ser maior que zero |
| `HorasTrabalho` | Deve ser maior que zero |
| `NivelMecanico` | Encapsula lógica de promoção e multiplicador de custo |
| `StatusOrdemServico` | Encapsula a máquina de estados com transições válidas |
| `PrazoConclusao` | Data futura; expõe `horasRestantes()` |
| `OrcamentoId`, `InsumoId`, `UsuarioId`, `VeiculoId`, `OrdemServicoId` | Identidades tipadas (evitam troca acidental de IDs) |

---

### Aggregates

#### Aggregate Root: `OrdemServico`

O único ponto de entrada para modificar o estado de uma OS e seu orçamento. O `Orcamento` é parte interna do agregado e só pode ser acessado e modificado através da `OrdemServico`.

```
OrdemServico (Aggregate Root)
├── Orcamento          ← parte do agregado, acesso somente via OrdemServico
├── List<InsumoId>     ← referências por ID (insumos são agregados independentes)
├── StatusOrdemServico ← VO com máquina de estados
└── PrazoConclusao?    ← VO de prazo estimado
```

Os demais conceitos (`Usuario`, `Veiculo`, `Insumo`, `Mecanico`) são aggregates independentes, referenciados dentro da `OrdemServico` apenas por seus IDs.

---

## 3. Regras de negócio

### Ordem de Serviço
- Uma OS é sempre criada com status `RECEBIDA`.
- O motivo da OS não pode ser vazio.
- Insumos só podem ser adicionados ou removidos quando o status é `EM_DIAGNOSTICO`.
- O orçamento só pode ser gerado quando o status é `EM_DIAGNOSTICO`; ao ser gerado, o status avança automaticamente para `AGUARDANDO_APROVACAO`.
- A aprovação do orçamento só é possível com status `AGUARDANDO_APROVACAO`; ao aprovar, o status avança para `EM_EXECUCAO`.
- A reprovação do orçamento cancela a OS automaticamente (`CANCELADA`).
- O prazo de conclusão não pode ser uma data no passado e só pode ser definido uma vez.
- Um mecânico só pode ser reatribuído se o novo for diferente do atual.

### Orçamento
- O orçamento é composto por custo de mão de obra (`custoMaoDeObra`) e custo de insumos (`custoInsumos`); o valor total é calculado como a soma dos dois.
- Um orçamento já aprovado não pode ser reprovado.
- Um orçamento já aprovado não pode ser aprovado novamente.
- A reprovação exige um motivo não vazio.

### Insumo / Controle de Estoque
- A quantidade de estoque não pode ficar negativa após uma reserva.
- A quantidade a reservar ou adicionar deve ser maior que zero.
- O estoque mínimo é configurável por insumo; `estaAbaixoDoEstoqueMinimo()` sinaliza necessidade de reposição.
- O custo total de um insumo para uma quantidade é calculado como `custo unitário × quantidade`.

### Mecânico
- A promoção de nível segue a sequência: `JUNIOR → PLENO → SENIOR`.
- Mecânicos no nível `SENIOR` não podem ser promovidos.
- A promoção retorna um novo `Mecanico` (imutabilidade); o original não é alterado.
- O custo da mão de obra é calculado como: `valorHora × horas × multiplicadorDoNivel`.

### Usuário
- Um usuário deve ter pelo menos um perfil (`Role`) associado.
- O nome não pode ser vazio.
- Um usuário desativado não pode ser desativado novamente, e um ativo não pode ser ativado novamente.

### Veículo
- Modelo, marca e ano são obrigatórios; o ano deve ser maior ou igual a 1900.
- A nova placa ao alterar deve ser diferente da atual.
- O novo proprietário ao transferir deve ser diferente do atual.

---

## 4. Estados e ciclo de vida da Ordem de Serviço

```
                    ┌─────────────────────────────────────┐
                    │                                     │
         abrir()    ▼        iniciarDiagnostico()         │
  ──────► RECEBIDA ──────► EM_DIAGNOSTICO                 │
                                  │                       │ cancelar()
                                  │ gerarOrcamento()      │ (qualquer etapa)
                                  ▼                       │
                        AGUARDANDO_APROVACAO ─────────────┤
                          │           │                   │
              aprovar()   │           │ reprovar()        │
                          ▼           ▼                   │
                     EM_EXECUCAO   CANCELADA ◄────────────┘
                          │
                finalizar()│
                          ▼
                      FINALIZADA
                          │
                 entregar()│
                          ▼
                       ENTREGUE
```

| Status | Descrição |
|---|---|
| `RECEBIDA` | OS aberta, aguardando início do diagnóstico |
| `EM_DIAGNOSTICO` | Mecânico avaliando o veículo; insumos podem ser adicionados |
| `AGUARDANDO_APROVACAO` | Orçamento gerado e enviado ao cliente para aprovação |
| `EM_EXECUCAO` | Cliente aprovou; serviço em andamento |
| `FINALIZADA` | Serviço concluído; aguardando entrega ao cliente |
| `ENTREGUE` | Veículo entregue ao cliente; estado final |
| `CANCELADA` | OS encerrada sem conclusão; estado final |

---

## 5. Invariantes

Invariantes são condições que nunca podem ser violadas, independentemente do fluxo de execução.

1. **Uma OS nunca pode ter status inválido** — toda transição passa pela máquina de estados em `StatusOrdemServico`; transições não mapeadas lançam `IllegalStateException`.
2. **O estoque de um insumo nunca pode ficar negativo** — `reservar()` valida antes de decrementar.
3. **Um orçamento aprovado é irreversível** — `reprovar()` em um orçamento já aprovado lança `IllegalStateException`.
4. **Insumos não podem ser manipulados fora do diagnóstico** — qualquer tentativa fora de `EM_DIAGNOSTICO` lança `IllegalStateException`.
5. **A OS sempre inicia como `RECEBIDA`** — o factory `abrir()` impõe este estado; não há outro caminho de criação.
6. **O prazo de conclusão é imutável após definição** — `definirPrazoConclusao()` rejeita redefinição.
7. **Um `ValorMonetario` nunca pode ser negativo** — validado no `init` do Value Object.
8. **Um `Cpf` inválido nunca instancia** — dígitos verificadores calculados e validados no `init`.
9. **Uma `OrdemServico` sem motivo nunca instancia** — factory `abrir()` exige motivo não vazio.
10. **Mecânico `SENIOR` não pode ser promovido** — `proximoNivel()` lança `DomainException`.

---

## 6. Exemplos de código

### Entidade com comportamento — `OrdemServico`

```kotlin
val os = OrdemServico.abrir(
    motivo = "Barulho no motor ao acelerar",
    clienteId = UsuarioId.gerar(),
    mecanicoId = UsuarioId.gerar(),
    veiculoId = VeiculoId.gerar()
)

os.iniciarDiagnostico()

os.adicionarInsumo(InsumoId.de("id-filtro-oleo"))
os.adicionarInsumo(InsumoId.de("id-oleo-5w30"))

os.gerarOrcamento(
    custoMaoDeObra = ValorMonetario(BigDecimal("180.00")),
    custoInsumos   = ValorMonetario(BigDecimal("95.00"))
)

os.aprovarOrcamento()

os.finalizar()
os.entregar()
```

### Enum de status com descrição

```kotlin
enum class StatusOrdemServicoEnum(val ordem: Int, val descricao: String) {
    CANCELADA(0, "Cancelada"),
    RECEBIDA(1, "Recebida"),
    EM_DIAGNOSTICO(2, "Em Diagnóstico"),
    AGUARDANDO_APROVACAO(3, "Aguardando Aprovação"),
    EM_EXECUCAO(4, "Em Execução"),
    FINALIZADA(5, "Finalizada"),
    ENTREGUE(6, "Entregue")
}
```

### Value Object com máquina de estados

```kotlin
@JvmInline
value class StatusOrdemServico private constructor(val valor: StatusOrdemServicoEnum) {

    fun podeTransitarPara(novoStatus: StatusOrdemServicoEnum): Boolean =
        when (valor) {
            StatusOrdemServicoEnum.RECEBIDA ->
                novoStatus in listOf(EM_DIAGNOSTICO, CANCELADA)
            StatusOrdemServicoEnum.EM_DIAGNOSTICO ->
                novoStatus in listOf(AGUARDANDO_APROVACAO, CANCELADA)
            StatusOrdemServicoEnum.AGUARDANDO_APROVACAO ->
                novoStatus in listOf(EM_EXECUCAO, CANCELADA)
            StatusOrdemServicoEnum.EM_EXECUCAO ->
                novoStatus in listOf(FINALIZADA, CANCELADA)
            StatusOrdemServicoEnum.FINALIZADA ->
                novoStatus == ENTREGUE
            else -> false
        }

    fun transitarPara(novoStatus: StatusOrdemServicoEnum): StatusOrdemServico {
        if (!podeTransitarPara(novoStatus))
            throw IllegalStateException("Transição inválida de $valor para $novoStatus")
        return StatusOrdemServico(novoStatus)
    }
}
```

### Regra de negócio encapsulada — cálculo de custo do mecânico

```kotlin
fun calcularCusto(horas: HorasTrabalho): ValorMonetario {
    val total = valorHora.valor
        .multiply(horas.valor.toBigDecimal())
        .multiply(nivel.multiplicador().toBigDecimal())
        .setScale(2, RoundingMode.HALF_UP)
    return ValorMonetario(total)
}
```

### Controle de estoque com invariante

```kotlin
fun reservar(qtdNecessaria: Int) {
    if (qtdNecessaria <= 0)
        throw DomainException("A quantidade necessária deve ser maior que zero.")
    if (qtdNecessaria > qtdEstoque)
        throw DomainException("Quantidade necessária ($qtdNecessaria) excede o estoque disponível ($qtdEstoque).")
    qtdEstoque -= qtdNecessaria
    dataAtualizacao = LocalDateTime.now()
}
```

---

## 7. Restrições da camada

O domínio **não deve conter** nenhum dos itens abaixo. A presença de qualquer um deles é um indicativo de violação arquitetural.

| Categoria | Exemplos proibidos |
|---|---|
| Frameworks web | Spring MVC, Quarkus REST, JAX-RS, anotações `@RestController`, `@Path` |
| Persistência | JPA, Hibernate, `@Entity`, `@Repository`, JDBC, Panache |
| Injeção de dependência | `@Inject`, `@Autowired`, `@ApplicationScoped` |
| Serialização | Jackson, Gson, `@JsonProperty`, `ObjectMapper` |
| Infraestrutura de rede | HTTP clients, sockets, mensageria (Kafka, RabbitMQ) |
| I/O | Leitura de arquivos, variáveis de ambiente, configurações externas |
| Validação de framework | Bean Validation (`@NotNull`, `@Size`) — validações devem estar no `init` dos VOs |
| Logging externo | `Logger`, `log.info()` — use eventos de domínio se necessário |

---

## 8. Decisões arquiteturais

### Isolamento total do domínio
O módulo `_domain` possui apenas dependências de teste (`junit-jupiter`, `kotlin-test`). Nenhuma dependência de runtime externo é declarada. Isso garante que o domínio possa ser testado de forma completamente isolada e que nenhum detalhe de infraestrutura vaze para as regras de negócio.

### Construtor privado + factory method
Todas as entidades expõem apenas factory methods (`abrir()`, `criar()`, `gerar()`) como ponto de criação. O construtor privado garante que nenhum objeto seja criado em estado inválido por código externo.

```kotlin
class OrdemServico private constructor(...) {
    companion object {
        fun abrir(motivo: String, ...): OrdemServico { ... }
    }
}
```

### Value Objects com `@JvmInline`
Os VOs utilizam `@JvmInline value class` para evitar overhead de boxing na JVM, mantendo a semântica de imutabilidade e type-safety sem custo de performance. Isso impede, por exemplo, que um `UsuarioId` seja passado onde se espera um `VeiculoId`.

### Máquina de estados no Value Object
A lógica de transição de status da OS está encapsulada em `StatusOrdemServico` (um VO), não na entidade `OrdemServico` nem em um service externo. Isso garante que a regra viva próxima ao dado que ela protege e que não possa ser ignorada por nenhum caminho de código.

### Orçamento como parte do agregado `OrdemServico`
O `Orcamento` não possui repositório próprio nem é acessado diretamente. Ele é criado, aprovado e reprovado exclusivamente através dos métodos de `OrdemServico`. Isso mantém a consistência entre o status da OS e o estado do orçamento — aprovar o orçamento e avançar para `EM_EXECUCAO` são operações atômicas dentro do mesmo agregado.

### Referência a outros aggregates por ID
A `OrdemServico` referencia `Usuario`, `Veiculo` e `Insumo` somente por seus IDs tipados (`UsuarioId`, `VeiculoId`, `InsumoId`). Isso respeita os limites dos aggregates e evita acoplamento estrutural entre agregados distintos.

### Exceções de domínio tipadas
`DomainException` é a exceção base para violações de regras de negócio explícitas. `IllegalStateException` é utilizada para violações de invariante de estado (ex: transição inválida, reaprovação de orçamento). `IllegalArgumentException` é utilizada para pré-condições de criação (via `require()`). Essa distinção facilita o tratamento diferenciado nas camadas externas.
