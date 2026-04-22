# Módulo `_infrastructure`

Camada de adaptadores do ServiceTrack API. É a única camada que conhece o framework Quarkus, o banco de dados, o protocolo HTTP e os mecanismos de segurança. Traduz requisições externas em chamadas para os casos de uso da camada `_application` e persiste ou recupera dados do banco sem expor detalhes de infraestrutura ao domínio.

---

## Responsabilidades

- **Controladores REST**: expor endpoints HTTP, aplicar validações de entrada e delegar para os casos de uso.
- **Persistência**: implementar os `RepositoryPort` definidos em `_application`, mapeando entidades JPA ↔ objetos de domínio.
- **Autenticação e autorização**: emitir e validar tokens JWT via SmallRye JWT; aplicar `@RolesAllowed` nos endpoints.
- **Proxy de auditoria**: interceptar chamadas anotadas com `@Auditavel` e registrar eventos de auditoria de forma transparente.
- **Wiring de dependências**: instanciar e injetar implementações dos casos de uso por meio de classes de configuração CDI.
- **Geração de código de contrato**: compilar as definições OpenAPI YAML em interfaces Java utilizadas como base dos controllers.

---

## Tecnologias Principais

| Tecnologia | Uso |
|---|---|
| Quarkus 3.x | Runtime, CDI, configuração, servidor HTTP embutido |
| Hibernate ORM + Panache (Kotlin) | Mapeamento ORM e repositórios com `PanacheEntity` |
| RESTEasy Reactive + Jackson | Serialização JSON e binding HTTP |
| SmallRye JWT | Emissão e validação de tokens JWT (RS256) |
| Hibernate Validator | Validação de DTOs de entrada (`@Valid`, `@NotNull`, etc.) |
| H2 (testes) | Banco em memória com `drop-and-create` nos testes de integração |
| RestAssured + `@QuarkusTest` | Testes de integração com contexto Quarkus completo |
| openapi-generator | Geração de interfaces Java e DTOs a partir dos arquivos YAML |

---

## Estrutura de Pacotes

```
infrastructure/
  autenticacao/     → Emissão de tokens JWT e endpoint de login
  usuario/          → Adapter de persistência e resource de usuários
  mecanico/         → Adapter de persistência e resource de mecânicos
  veiculo/          → Adapter de persistência e resource de veículos
  servico/          → Adapter de persistência e resource de serviços
  insumo/           → Adapter de persistência e resource de insumos
  catalogo/         → Resource de catálogo (serviços e insumos disponíveis)
  ordemServico/     → Adapter de persistência, resource e mappers de OS
  auditoria/        → Entidade JPA de auditoria, adapter de repositório e proxy AOP
  config/           → Classes de configuração CDI (wiring dos casos de uso)
  api/              → Interfaces geradas pelo openapi-generator (contratos REST)
```

Cada contexto segue o mesmo padrão de três arquivos:

- `*Entity.kt` — entidade JPA com mapeamento de colunas e conversão para domínio (`.toDomain()`).
- `*RepositoryAdapter.kt` — implementação do `RepositoryPort` definido em `_application`.
- `*ResourceImpl.kt` — implementação da interface gerada pelo OpenAPI, com anotações de segurança.
- `*Mapper.kt` — funções de extensão para converter DTOs de request em DTOs de aplicação.
- `*ServiceConfig.kt` — classe `@ApplicationScoped` que instancia os casos de uso via `AuditoriaProxy.envolver()`.

---

## Fluxo de uma Requisição HTTP

```
Cliente HTTP
    │
    ▼
RESTEasy (binding + validação @Valid)
    │
    ▼
*ResourceImpl (@RolesAllowed → SmallRye JWT verifica token)
    │  converte request DTO → application DTO via Mapper
    ▼
AuditoriaProxy (intercepta @Auditavel; captura estado anterior)
    │
    ▼
*UseCase (interface de _application)
    │  instanciado por *ServiceConfig com dependências injetadas
    ▼
*Service (implementação em _application)
    │  chama RepositoryPort, domínio, etc.
    ▼
*RepositoryAdapter (implementação em _infrastructure)
    │  converte domínio ↔ entidade JPA
    ▼
Panache / Hibernate ORM → PostgreSQL (produção) / H2 (testes)
```

Após a execução do caso de uso, o `AuditoriaProxy` persiste o evento de auditoria com o estado anterior e posterior da entidade.

---

## Proxy de Auditoria

O mecanismo de auditoria é implementado por `AuditoriaProxy`, que envolve cada caso de uso num proxy JDK dinâmico. Métodos anotados com `@Auditavel` têm seu estado anterior capturado antes da execução e o estado posterior registrado após. Nenhum serviço de negócio precisa conhecer esse comportamento — a anotação é suficiente.

Toda instanciação de caso de uso em `*ServiceConfig.kt` passa por `AuditoriaProxy.envolver(useCase, ...)`.

---

## Contrato de API (Contract-First)

O contrato REST é definido em arquivos YAML dentro de `openApi/` e processado pelo plugin `openapi-generator` durante o build do Gradle. O resultado são interfaces Java em `build/generated/openapi/` que os `*ResourceImpl.kt` implementam. Isso garante que o contrato seja a fonte de verdade e que qualquer divergência cause falha de compilação.

---

## Boas Práticas

- **Isolamento do domínio**: as entidades JPA e os DTOs de request/response nunca são expostos para `_application` ou `_domain`. A conversão é sempre explícita, em funções de extensão nos Mappers.
- **Injeção de dependência**: use `@ApplicationScoped` + `@Inject` nas classes de infraestrutura. Os casos de uso são instanciados manualmente nas classes `*ServiceConfig` para que o proxy de auditoria seja aplicado corretamente.
- **Transações declarativas**: use `@Transactional` nos métodos do resource que modificam estado. Operações de leitura não precisam da anotação.
- **Validação na borda**: toda validação de formato e obrigatoriedade de campos ocorre nos DTOs gerados pelo OpenAPI, via anotações Bean Validation. A camada de aplicação assume que os dados chegam válidos.
- **Testes de integração**: cada classe de IT deve usar CPFs únicos para evitar colisões no schema H2 compartilhado entre testes do mesmo módulo.
