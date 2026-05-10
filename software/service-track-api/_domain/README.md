# _domain — ServiceTrack API

Núcleo do sistema. Contém **todo o conhecimento do negócio** da oficina mecânica: entidades, value objects, regras de negócio, invariantes e a linguagem ubíqua do domínio. Não possui nenhuma dependência de framework ou infraestrutura.

---

## 1. Subdomínios

| Subdomínio | Pacote | Papel |
|---|---|---|
| Ordem de Serviço | `domain.ordemServico` | **Core Domain** — coração do negócio |
| Orçamento | `domain.orcamento` | Core Domain — parte integrante da OS |
| Mecânico | `domain.mecanico` | Supporting — gestão do profissional |
| Veículo | `domain.veiculo` | Supporting — ativo sobre o qual a OS é aberta |
| Usuário | `domain.usuario` | Supporting — clientes e mecânicos |
| Serviço | `domain.servico` | Generic — catálogo de serviços oferecidos |
| Insumo | `domain.insumo` | Generic — catálogo de peças e materiais |
| Auditoria | `domain.auditoria` | Cross-cutting — rastreabilidade de operações |

---

## 2. Linguagem Ubíqua

| Termo | Significado no domínio |
|---|---|
| **Ordem de Serviço (OS)** | Registro de um atendimento à um veículo, desde a abertura até a entrega |
| **Item de OS** | Um serviço específico associado a uma OS, com mecânico responsável |
| **Orçamento** | Estimativa de custo (mão de obra + insumos) gerada durante o diagnóstico |
| **Diagnóstico** | Fase em que o mecânico inspeciona o veículo e registra serviços e insumos |
| **Mecânico** | Profissional que executa os serviços; possui nível (JUNIOR, PLENO, SENIOR) |
| **Insumo** | Peça ou material utilizado durante a execução de um serviço |
| **Serviço** | Tipo de trabalho do catálogo (ex.: troca de óleo, alinhamento) |
| **Veículo** | Ativo do cliente sobre o qual a OS é aberta |
| **Cliente** | Proprietário do veículo; aprova ou reprova o orçamento |

---

## 3. Aggregate Roots e Entidades

### OrdemServico (Aggregate Root)

O aggregate raiz do domínio. Controla o ciclo de vida completo de um atendimento.

```kotlin
class OrdemServico private constructor(
    val id: OrdemServicoId,
    val motivo: String,
    var observacao: String,
    val clienteId: UsuarioId,
    private var mecanicoId: UsuarioId,
    val veiculoId: VeiculoId,
    val dataCriacao: LocalDateTime,
    var dataAtualizacao: LocalDateTime,
    private var status: StatusOrdemServico,   // value object com state machine
    private var prazoConclusao: PrazoConclusao?,
    private var orcamento: Orcamento?,
    private val insumos: MutableList<InsumoId>,
    private val itensServico: MutableList<ItemOrdemServico>,
)
```

A construção é bloqueada: `private constructor` + factory methods `abrir()` e `reconstituir()`. Isso garante que nenhuma OS entre em estado inválido.

**Regras de negócio encapsuladas:**

- Insumos e serviços só podem ser adicionados/removidos durante `EM_DIAGNOSTICO`
- Orçamento só pode ser gerado durante `EM_DIAGNOSTICO`
- Aprovação/reprovação só é possível em `AGUARDANDO_APROVACAO`
- Conclusão de item só é possível em `EM_EXECUCAO`
- Um serviço concluído não pode ser removido nem ter seu valor alterado
- Prazo de conclusão não pode estar no passado e não pode ser redefinido

---

### ItemOrdemServico (Entidade interna do Aggregate)

Representa a execução de um serviço específico dentro de uma OS.

```kotlin
class ItemOrdemServico(
    val id: ItemOrdemServicoId,
    val servicoId: ServicoId,
    val ordemServicoId: OrdemServicoId,
    var valor: ValorMonetario,
    var feito: Boolean,
    var mecanicoResponsavelId: UsuarioId?,
    var dataRealizacao: LocalDateTime?,
    var observacao: String?,
)
```

**Regras:** Um item só pode ser concluído se tiver mecânico vinculado e observação não vazia. Serviços já concluídos são imutáveis.

---

### Orcamento (Entidade interna do Aggregate)

```kotlin
class Orcamento(
    val id: OrcamentoId,
    val custoMaoDeObra: ValorMonetario,
    val custoInsumos: ValorMonetario,
    private var aprovado: Boolean,
    private var observacao: String,
)

val valorTotal: ValorMonetario get() = custoMaoDeObra.somar(custoInsumos)
```

**Regras:** Orçamento aprovado não pode ser reprovado. Reprovação exige motivo não vazio.

---

### Mecanico

Entidade independente que representa o profissional da oficina.

```kotlin
class Mecanico(
    val usuarioId: UsuarioId,
    private var valorHora: ValorHora,
    private var nivel: NivelMecanico      // JUNIOR | PLENO | SENIOR
)

fun calcularCusto(horas: HorasTrabalho): ValorMonetario
fun promover(): Mecanico   // retorna nova instância com nível promovido
```

O cálculo de custo aplica um multiplicador por nível: JUNIOR=1x, PLENO=2x, SENIOR=3x.

---

### Usuario

Representa clientes e mecânicos. O papel é definido pelo conjunto de `Role`.

```kotlin
class Usuario(
    val id: UsuarioId,
    private var nome: String,
    private var email: Email,
    private var senha: Senha,        // armazenado como hash
    private var cpf: Cpf,
    private var telefone: Telefone,
    private val roles: MutableSet<Role>  // CLIENTE | MECANICO
)

fun ehCliente(): Boolean
fun ehMecanico(): Boolean
fun criarCliente(...): Usuario  // factory method
fun criarMecanico(...): Usuario // factory method
```

---

### Veiculo

```kotlin
class Veiculo(
    val id: VeiculoId,
    private var proprietarioId: UsuarioId,
    private var placa: Placa,
    private var modelo: String,
    private var marca: String,
    private var ano: Int,
    private var ativo: IndicativoSimNao
)
```

**Regras:** Veículo desativado não pode ter dados ou placa alterados. Ano mínimo: 1900.

---

### Servico e Insumo

Entidades de catálogo. `Servico` representa os tipos de trabalho disponíveis; `Insumo`, as peças e materiais.

```kotlin
class Servico(
    val id: ServicoId,
    val nomeServico: String,
    val descricaoServico: String,
    var valorReferencia: ValorMonetario?,  // pode não ter valor fixo
)
```

---

## 4. Value Objects

| VO | Tipo Kotlin | Invariante |
|---|---|---|
| `ValorMonetario` | `@JvmInline value class` | Não negativo; operações `somar()` e `multiplicar()` |
| `Cpf` | `@JvmInline value class` | 11 dígitos + algoritmo de validação dos dois dígitos verificadores |
| `Placa` | `@JvmInline value class` | Regex `[A-Z]{3}[0-9][A-Z0-9][0-9]{2}` (padrão Mercosul) |
| `Email` | `@JvmInline value class` | Formato de e-mail válido |
| `Senha` | `@JvmInline value class` | Não pode ser vazia (hash armazenado) |
| `Telefone` | `@JvmInline value class` | Formato de telefone válido |
| `StatusOrdemServico` | `@JvmInline value class` | Encapsula a state machine de transição de status |
| `NivelMecanico` | `@JvmInline value class` | Encapsula nível e multiplicador; impede promoção de SENIOR |
| `ValorHora` | `@JvmInline value class` | Valor por hora do mecânico, não negativo |
| `HorasTrabalho` | `@JvmInline value class` | Horas trabalhadas, não negativo |
| `PrazoConclusao` | `data class` | Prazo não pode ser no passado |
| `OrdemServicoId`, `UsuarioId`, etc. | `@JvmInline value class` | IDs tipados (UUID gerado ou reconstituído) |

O uso de `@JvmInline value class` garante tipagem forte sem overhead de alocação em tempo de execução.

---

## 5. State Machine da Ordem de Serviço

A máquina de estados está encapsulada no Value Object `StatusOrdemServico`. Transições inválidas lançam `IllegalStateException` no domínio.

```
RECEBIDA
  ├─→ EM_DIAGNOSTICO
  └─→ CANCELADA

EM_DIAGNOSTICO
  ├─→ AGUARDANDO_APROVACAO
  └─→ CANCELADA

AGUARDANDO_APROVACAO
  ├─→ EM_EXECUCAO      (orçamento aprovado)
  └─→ CANCELADA        (orçamento reprovado)

EM_EXECUCAO
  ├─→ FINALIZADA
  └─→ CANCELADA

FINALIZADA
  └─→ ENTREGUE
```

---

## 6. Enums

| Enum | Valores | Localização |
|---|---|---|
| `StatusOrdemServicoEnum` | `RECEBIDA, EM_DIAGNOSTICO, AGUARDANDO_APROVACAO, EM_EXECUCAO, FINALIZADA, ENTREGUE, CANCELADA` | `domain.ordemServico` |
| `NivelMecanicoEnum` | `JUNIOR, PLENO, SENIOR` | `domain.mecanico` |
| `Role` | `CLIENTE, MECANICO` | `domain.shared.enums` |
| `IndicativoSimNao` | `S, N` | `domain.shared.enums` |
| `UnidadeTempoEnum` | Unidades de tempo para tempo médio de serviço | `domain.servico` |
| `TipoEventoAuditoria` | Tipos de evento auditável | `domain.auditoria.enums` |
| `TipoEntidade` | Entidades auditáveis | `domain.auditoria.enums` |

---

## 7. Exceções de domínio

`DomainException` é a base para todas as violações de regra de negócio no domínio. É mapeada pela `_infrastructure` para HTTP 400.

```kotlin
class DomainException(message: String) : RuntimeException(message)
```

---

## 8. Domínio rico

Este projeto pratica **Rich Domain Model**: as entidades não são anêmicas. Toda a lógica que diz respeito a uma entidade vive nela.

Exemplos concretos:

```kotlin
// A OS controla sua própria máquina de estados
os.iniciarDiagnostico()         // valida e transita RECEBIDA → EM_DIAGNOSTICO
os.gerarOrcamento(maoDeObra, insumos) // só funciona em EM_DIAGNOSTICO
os.aprovarOrcamento()           // transita AGUARDANDO_APROVACAO → EM_EXECUCAO

// O mecânico calcula seu próprio custo com o multiplicador de nível
mecanico.calcularCusto(HorasTrabalho(8))

// O CPF valida seus próprios dígitos verificadores
Cpf("123.456.789-09")  // lança DomainException se inválido

// A placa valida o formato Mercosul
Placa("ABC1D23")  // válido; Placa("abc123") → DomainException
```

---

## 9. Testes de domínio

Testes unitários puros em JUnit 5 + AssertJ, sem nenhuma dependência de framework.

**Cobertura por área:**

| Área | Arquivos de teste |
|---|---|
| `OrdemServico` (aggregate + state machine) | `OrdemServicoTest`, `StatusOrdemServicoTest` |
| `ItemOrdemServico` | `ItemOrdemServicoTest` |
| `Orcamento` | `OrcamentoTest` |
| `Mecanico` | `MecanicoTest`, `NivelMecanicoTest`, `ValorHoraTest`, `HorasTrabalhoTest` |
| `Usuario` | `UsuarioTest`, `CpfTest`, `EmailTest`, `SenhaTest`, `TelefoneTest` |
| `Veiculo` | `VeiculoTest`, `PlacaTest`, `DadosVeiculoTest` |
| `Servico` | `ServicoTest` |
| `Insumo` | `InsumoTest` |
| `ValorMonetario` | `ValorMonetarioTest` |
| `Auditoria` | `AuditoriaTest`, `EventoAuditoriaTest`, `CampoAlteradoTest`, VOs de auditoria |

```bash
cd software/service-track-api
./gradlew :_domain:test
./gradlew :_domain:jacocoTestReport
```
