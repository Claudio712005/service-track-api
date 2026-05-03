# _application — ServiceTrack API

Camada de orquestração de casos de uso. Coordena o fluxo entre a camada de domínio e os adaptadores de infraestrutura sem conter regra de negócio. Define os contratos (ports) que a infraestrutura deve implementar.

---

## 1. Responsabilidade

- Implementar os casos de uso do sistema (um serviço por caso de uso)
- Definir os contratos de entrada (`ports/in`) e saída (`ports/out`)
- Transformar dados entre o domínio e o mundo externo via DTOs e mappers
- Lançar exceções de aplicação quando operações não podem ser realizadas
- Coordenar auditoria via anotação declarativa

Esta camada **não contém regra de negócio**. Toda decisão de domínio fica em `_domain`. Esta camada apenas orquestra: carrega entidades, chama métodos de domínio, persiste e retorna.

---

## 2. Módulos da aplicação

A camada cobre os seguintes contextos:

| Contexto | Casos de uso |
|---|---|
| `ordemServico` | Criar, buscar, listar, cancelar, enviar para diagnóstico, associar itens, gerar orçamento, aprovar/reprovar orçamento, concluir item, finalizar, entregar |
| `mecanico` | Cadastrar, buscar, listar, atualizar |
| `usuario` | Criar usuário/cliente, login, buscar cliente |
| `veiculo` | Cadastrar, buscar, listar, atualizar, remover |
| `servico` | Criar, buscar, listar, atualizar, remover, buscar tempo médio de conclusão |
| `insumo` | Criar, buscar, listar, atualizar, remover |
| `auditoria` | Registrar e consultar eventos de auditoria (cross-cutting) |

---

## 3. Application Services

Cada caso de uso tem uma interface (port in) e uma implementação (service). A anotação `@ApplicationService` é uma meta-anotação que marca as classes de serviço para o container de injeção de dependência.

**Exemplo — ciclo completo de OS:**

```
CriarOrdemServicoUseCase    ← interface (port in)
CriarOrdemServicoService    ← implementação

EnviarParaDiagnosticoUseCase
EnviarParaDiagnosticoService

GerarOrcamentoUseCase
GerarOrcamentoService

AprovarOrcamentoUseCase
AprovarOrcamentoService

ReprovarOrcamentoUseCase
ReprovarOrcamentoService

ConcluirItemServicoUseCase
ConcluirItemServicoService

FinalizarOrdemServicoUseCase
FinalizarOrdemServicoService

EntregarOrdemServicoUseCase
EntregarOrdemServicoService
```

---

## 4. Ports

### Ports de entrada (`ports/in`)

Interfaces que definem o que o sistema consegue fazer. São implementadas pelos services e chamadas pelos adapters de entrada (REST resources).

```kotlin
interface CriarOrdemServicoUseCase {
    fun executar(dto: OrdemServicoReqDTO): OrdemServicoResDTO
}

interface AprovarOrcamentoUseCase {
    fun executar(ordemServicoId: UUID): OrcamentoResDTO
}

interface GerarOrcamentoUseCase {
    fun executar(ordemServicoId: UUID): OrcamentoResDTO
}
```

### Ports de saída (`ports/out`)

Interfaces que definem o que a aplicação precisa de infraestrutura. São implementadas pelos adapters de infraestrutura (repositories, JWT, criptografia).

| Port | Propósito |
|---|---|
| `OrdemServicoRepositoryPort` | Persistência de OS |
| `ItemOrdemServicoRepositoryPort` | Persistência de itens de OS |
| `MecanicoRepositoryPort` | Persistência de mecânicos |
| `UsuarioRepositoryPort` | Persistência de usuários |
| `VeiculoRepositoryPort` | Persistência de veículos |
| `ServicoRepositoryPort` | Persistência de serviços |
| `InsumoRepositoryPort` | Persistência de insumos |
| `AuditoriaRepositoryPort` | Persistência de registros de auditoria |
| `RegistrarAuditoriaPort` | Trigger de registro de auditoria |
| `JwtPort` | Geração e validação de tokens JWT |
| `CriptografiaPort` | Hash e verificação de senha |

---

## 5. DTOs

Os DTOs são estruturas de dados sem comportamento. Seguem a convenção:

- `*ReqDTO` — entrada (request)
- `*ResDTO` — saída (response)
- `*PatchDTO` — atualização parcial

**Exemplos relevantes:**

```kotlin
// Criação de OS
data class OrdemServicoReqDTO(
    val motivo: String,
    val clienteId: UUID,
    val mecanicoId: UUID,
    val veiculoId: UUID,
    val observacao: String?
)

// Filtro de listagem com paginação
data class FiltroOrdemServicoDTO(
    val status: String?,
    val clienteId: UUID?,
    val page: Int,
    val size: Int
)

// Resposta paginada genérica
data class PageResDTO<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)
```

---

## 6. Mappers

Responsáveis pela conversão entre entidades de domínio e DTOs. São classes sem estado.

| Mapper | Converte |
|---|---|
| `MecanicoMapper` | `Mecanico` ↔ `MecanicoResDTO` |
| `UsuarioMapper` | `Usuario` ↔ `ClienteResDTO` |
| `VeiculoMapper` | `Veiculo` ↔ `DadosVeiculoResDTO` |

---

## 7. Exceções de aplicação

As exceções de aplicação representam violações de contrato de negócio detectadas fora do domínio puro (ex.: unicidade, autorização). São mapeadas pela infraestrutura para respostas HTTP específicas.

| Exceção | HTTP |
|---|---|
| `EntidadeNaoEncontradaException` | 404 Not Found |
| `OperacaoNegadaException` | 403 Forbidden |
| `CredenciaisInvalidasException` | 401 Unauthorized |
| `UsuarioJaExisteException` | 409 Conflict |
| `VeiculoJaExisteException` | 409 Conflict |

---

## 8. Auditoria declarativa

A auditoria é implementada de forma declarativa com a anotação `@Auditavel`. O interceptor de infraestrutura detecta a anotação e registra o evento após a execução do método.

```kotlin
@Auditavel(
    entidade = TipoEntidade.MECANICO,
    evento = TipoEventoAuditoria.CRIACAO
)
fun executar(dto: CadastrarMecanicoReqDTO): MecanicoResDTO
```

O contexto antes da operação é armazenado em `AuditoriaContextoHolder` (thread-local), disponível para o interceptor no momento do registro.

---

## 9. Testes da camada

Testes unitários com **MockK** (mocking idiomático para Kotlin). Cada service tem seu teste correspondente, verificando:

- Fluxo feliz: orquestração correta e delegação ao domínio
- Casos de erro: exceções corretas quando entidade não existe ou operação é inválida
- Chamadas corretas aos ports de saída (verify)

**Cobertura por contexto:**

| Contexto | Testes |
|---|---|
| OrdemServico | `CriarOrdemServicoServiceTest`, `AprovarOrcamentoServiceTest`, `GerarOrcamentoServiceTest`, `ReprovarOrcamentoServiceTest`, `ConcluirItemServicoServiceTest`, `FinalizarOrdemServicoServiceTest`, `EntregarOrdemServicoServiceTest`, `CancelarOrdemServicoServiceTest`, `EnviarParaDiagnosticoServiceTest`, `AssociarItensOrdemServicoServiceTest` |
| Mecânico | `CadastrarMecanicoServiceTest`, `AtualizarMecanicoServiceTest`, `BuscarMecanicoServiceTest`, `ListarMecanicosServiceTest` |
| Usuário | `CriarUsuarioServiceTest`, `LoginServiceTest` |
| Veículo | `CadastrarVeiculoServiceTest`, `AtualizarVeiculoServiceTest`, `BuscarVeiculoServiceTest`, `ListarVeiculosServiceTest`, `RemoverVeiculoServiceTest` |
| Serviço | `CriarServicoServiceTest`, `AtualizarServicoServiceTest`, `BuscarServicoServiceTest`, `ListarServicosServiceTest`, `RemoverServicoServiceTest`, `BuscarTempoMedioConclusaoServiceTest` |
| Insumo | `CriarInsumoServiceTest`, `AtualizarInsumoServiceTest`, `BuscarInsumoServiceTest`, `ListarInsumosServiceTest`, `RemoverInsumoServiceTest` |

```bash
cd software/service-track-api
./gradlew :_application:test
./gradlew :_application:jacocoTestReport
```
