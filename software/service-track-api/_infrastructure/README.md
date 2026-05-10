# _infrastructure — ServiceTrack API

Camada de adaptadores. É a única camada que conhece o Quarkus, o banco de dados, o protocolo HTTP e os mecanismos de segurança. Implementa todos os ports definidos em `_application` e expõe a API REST gerada a partir do contrato OpenAPI.

---

## 1. Responsabilidade

- Receber requisições HTTP e traduzi-las em chamadas aos casos de uso
- Implementar os ports de saída (repositórios, JWT, criptografia)
- Configurar o container de injeção de dependência (CDI)
- Mapear exceções de domínio e aplicação para respostas HTTP
- Interceptar operações auditáveis e registrar eventos de auditoria

Esta camada **não toma decisões de negócio**. Se precisa de uma regra, delega ao domínio via `_application`.

---

## 2. REST Resources

O projeto adota **contract-first**: as interfaces REST são geradas pelo OpenAPI Generator a partir dos contratos em `openApi/`. As implementações estão em `_infrastructure`.

| Resource | Contexto |
|---|---|
| `AutenticacaoResourceImpl` | Login, cadastro de cliente |
| `MecanicoResourceImpl` | CRUD de mecânicos |
| `ClienteResourceImpl` | Consulta de clientes |
| `VeiculoResourceImpl` | CRUD de veículos |
| `ServicoResourceImpl` | CRUD de serviços do catálogo |
| `InsumoResourceImpl` | CRUD de insumos do catálogo |
| `OrdemServicoResourceImpl` | Criação e todo o ciclo de vida da OS |
| `CatalogoResourceImpl` | Endpoints de consulta ao catálogo (serviços + insumos) |

Cada resource injeta o(s) use case(s) correspondente(s) e delega sem lógica adicional:

```kotlin
@Path("/ordens-servico")
class OrdemServicoResourceImpl(
    private val criarOrdemServico: CriarOrdemServicoUseCase,
    private val aprovarOrcamento: AprovarOrcamentoUseCase,
    // ...
) : OrdemServicoApi {

    override fun criar(dto: OrdemServicoReqDTO): Response =
        Response.status(201).entity(criarOrdemServico.executar(dto)).build()
}
```

---

## 3. Persistência

### JPA Entities

Cada entidade de domínio tem uma entidade JPA correspondente. A conversão domain ↔ entity é feita no `RepositoryAdapter`.

| JPA Entity | Entidade de domínio |
|---|---|
| `OrdemServicoEntity` | `OrdemServico` |
| `ItemOrdemServicoEntity` | `ItemOrdemServico` |
| `OrcamentoEntity` / `OrcamentoEmbeddable` | `Orcamento` |
| `MecanicoEntity` | `Mecanico` |
| `UsuarioEntity` | `Usuario` |
| `VeiculoEntity` | `Veiculo` |
| `ServicoEntity` | `Servico` |
| `InsumoEntity` | `Insumo` |
| `AuditoriaEntity` | `Auditoria` |

### Repository Adapters

Implementam os ports de saída definidos em `_application`. Traduzem entre o modelo de domínio e o modelo de persistência.

```
OrdemServicoRepositoryAdapter  implements  OrdemServicoRepositoryPort
MecanicoRepositoryAdapter      implements  MecanicoRepositoryPort
UsuarioRepositoryAdapter       implements  UsuarioRepositoryPort
VeiculoRepositoryAdapter       implements  VeiculoRepositoryPort
ServicoRepositoryAdapter       implements  ServicoRepositoryPort
InsumoRepositoryAdapter        implements  InsumoRepositoryPort
AuditoriaRepositoryAdapter     implements  AuditoriaRepositoryPort
```

### Banco de dados por perfil

| Perfil Quarkus | Banco |
|---|---|
| `dev` | H2 in-memory (`jdbc:h2:mem:servicetrack`) |
| `test` | H2 in-memory (recriado a cada execução de IT) |
| `prod` | PostgreSQL 16 (via variável `QUARKUS_DATASOURCE_JDBC_URL`) |

Schema gerenciado pelo Hibernate com `quarkus.hibernate-orm.database.generation=update`.

---

## 4. Segurança

### JWT (RS256)

`JwtAdapter` implementa `JwtPort` usando SmallRye JWT. Gera tokens assinados com chave privada RSA e os valida com a chave pública.

```
mp.jwt.verify.issuer=service-track-api
mp.jwt.verify.publickey.location=classpath:keys/publicKey.pem
mp.jwt.verify.publickey.algorithm=RS256
smallrye.jwt.sign.key.location=classpath:keys/privateKey.pem
servicetrack.jwt.expiracao-horas=8
```

### BCrypt

`BcryptCriptografiaAdapter` implementa `CriptografiaPort`. Usado para hash de senha no cadastro e verificação no login.

### Mapeamento de exceções para HTTP

`ExceptionMappers` (JAX-RS `@Provider`) converte cada exceção em resposta HTTP estruturada:

| Exceção | HTTP |
|---|---|
| `DomainException` | 400 Bad Request |
| `ConstraintViolationException` | 400 Bad Request |
| `EntidadeNaoEncontradaException` | 404 Not Found |
| `CredenciaisInvalidasException` | 401 Unauthorized |
| `OperacaoNegadaException` | 403 Forbidden |
| `UsuarioJaExisteException` | 409 Conflict |
| `VeiculoJaExisteException` | 409 Conflict |

Resposta padronizada:

```json
{
  "mensagem": "Recurso não encontrado",
  "detalhe": "Ordem de serviço com id X não encontrada"
}
```

---

## 5. Auditoria

A auditoria utiliza um interceptor CDI (`AuditoriaInterceptor`) acionado pela anotação `@AuditavelInterceptorBinding`. O fluxo é:

1. O resource ou service anota o método com `@Auditavel(entidade, evento)`
2. Antes da execução, o estado anterior pode ser armazenado em `AuditoriaContextoHolder`
3. Após a execução, o interceptor extrai o ID do resultado e registra o evento via `RegistrarAuditoriaPort`
4. O `RegistrarAuditoriaAdapter` persiste o registro em `AuditoriaEntity`

O padrão Strategy (`AuditoriaStrategy`) determina como cada tipo de evento (criação, atualização, remoção, login) monta o registro:

```
AuditoriaStrategyFactory
  ├── CiacaoAuditoriaStrategy
  ├── AtualizacaoAuditoriaStrategy
  ├── RemocaoAuditoriaStrategy
  ├── LoginAuditoriaStrategy
  └── LogoutAuditoriaStrategy
```

---

## 6. Inversão de dependência

A infraestrutura depende das abstrações definidas em `_application`, nunca o contrário:

```
_application define:           _infrastructure implementa:
OrdemServicoRepositoryPort  ←  OrdemServicoRepositoryAdapter
JwtPort                     ←  JwtAdapter
CriptografiaPort            ←  BcryptCriptografiaAdapter
RegistrarAuditoriaPort      ←  RegistrarAuditoriaAdapter
```

A configuração dos services de aplicação é feita em classes de configuração CDI (`*ServiceConfig`), que injetam os adapters nos constructors dos services da camada `_application`:

```kotlin
@ApplicationScoped
class OrdemServicoServiceConfig(
    private val ordemServicoRepo: OrdemServicoRepositoryPort,
    private val mecanicoRepo: MecanicoRepositoryPort,
    // ...
) {
    @Produces
    fun criarOrdemServicoService(): CriarOrdemServicoUseCase =
        CriarOrdemServicoService(ordemServicoRepo, mecanicoRepo, ...)
}
```

---

## 7. Testes de integração (IT)

Os testes de integração usam `@QuarkusTest` com banco H2 in-memory e RestAssured para chamadas HTTP. As chaves JWT são geradas via OpenSSL antes de cada execução no CI.

**Convenções:**
- Cada classe de IT testa um fluxo ou recurso completo
- CPFs e dados únicos são gerados por classe para evitar conflitos entre testes
- Testes de fluxo (`OrdemServicoFluxoIT`) verificam o ciclo completo de uma OS do início ao fim

| Teste IT | Cobertura |
|---|---|
| `LoginIT` | Autenticação e geração de token |
| `CadastrarClienteIT` | Cadastro de usuário cliente |
| `CadastrarMecanicoIT` | Cadastro de mecânico |
| `AtualizarMecanicoIT` | Atualização de dados do mecânico |
| `MecanicoIT` | Busca e listagem de mecânicos |
| `ClienteIT` | Consulta de clientes |
| `VeiculoIT` | CRUD completo de veículos |
| `ServicoIT` | CRUD completo de serviços |
| `InsumoIT` | CRUD completo de insumos |
| `CatalogoIT` | Consulta ao catálogo público |
| `OrdemServicoIT` | Criação e operações de OS |
| `OrdemServicoFluxoIT` | Fluxo completo: abertura → diagnóstico → orçamento → execução → entrega |
| `TempoMedioConclusaoIT` | Cálculo de tempo médio por serviço |
| `AuditoriaProxyTest` | Verificação do mecanismo de auditoria |

```bash
cd software/service-track-api

# Gerar chaves JWT para testes (necessário uma vez)
mkdir -p _infrastructure/src/test/resources/keys
openssl genrsa -out _infrastructure/src/test/resources/keys/privateKey.pem 2048
openssl rsa -in _infrastructure/src/test/resources/keys/privateKey.pem \
  -pubout -out _infrastructure/src/test/resources/keys/publicKey.pem

# Executar testes de integração
./gradlew :_infrastructure:test
./gradlew :_infrastructure:jacocoTestReport
```
