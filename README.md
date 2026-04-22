# ServiceTrack API

Sistema de gestão de ordens de serviço para oficina mecânica, desenvolvido como MVP com foco em organização operacional, rastreabilidade e eficiência no atendimento ao cliente.

---

## Sobre o Projeto

O ServiceTrack resolve problemas recorrentes em oficinas mecânicas:

- Falta de controle sobre o fluxo de ordens de serviço
- Gestão manual e imprecisa de estoque de insumos
- Dificuldade de comunicação sobre status entre mecânicos e clientes
- Ausência de histórico rastreável de serviços e orçamentos

A API oferece suporte a todo o ciclo de vida de uma ordem de serviço, desde o recebimento do veículo até a entrega ao cliente, incluindo diagnóstico, geração de orçamento, aprovação e finalização.

---

## Arquitetura

O sistema adota **Arquitetura Hexagonal** organizada como **Monolito Modular**, com separação clara de responsabilidades entre três módulos Gradle independentes:

```
_domain         → Entidades, regras de negócio, objetos de valor e exceções de domínio.
                  Sem dependências externas — puro Kotlin.

_application    → Casos de uso, ports (interfaces) e DTOs.
                  Orquestra o domínio sem conhecer detalhes de infraestrutura.

_infrastructure → Adaptadores: REST controllers, persistência, autenticação JWT,
                  proxy de auditoria e configuração de injeção de dependências.
                  Único módulo com dependências de framework (Quarkus).
```

O fluxo de dependência segue a direção: `_infrastructure → _application → _domain`.  
Nenhuma camada interna conhece a camada externa.

As decisões arquiteturais estão documentadas em ADRs e RFCs:

- [ADR-001 - Monolito Modular](docs/adr/ADR-001-monolito-modular.md) · [RFC-001](docs/rfc/RFC-001-monolito-modular.md)
- [ADR-002 - PostgreSQL](docs/adr/ADR-002-postgresql.md) · [RFC-002](docs/rfc/RFC-002-postgresql.md)
- [ADR-003 - Kotlin](docs/adr/ADR-003-kotlin.md) · [RFC-003](docs/rfc/RFC-003-kotlin.md)
- [ADR-004 - Quarkus](docs/adr/ADR-004-quarkus.md) · [RFC-004](docs/rfc/RFC-004-quarkus.md)
- [ADR-005 - Autenticação JWT](docs/adr/ADR-005-autenticacao-jwt.md) · [RFC-005](docs/rfc/RFC-005-autenticacao-jwt.md)

---

## Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.0.21 (compilador K2) |
| Framework | Quarkus 3.x |
| Persistência | Hibernate ORM + Panache · PostgreSQL (produção) · H2 (testes) |
| REST | RESTEasy Reactive + Jackson |
| Contrato de API | OpenAPI 3.0 (contract-first) · geração de código via `openapi-generator` |
| Autenticação | SmallRye JWT · RSA-256 (par de chaves assimétricas) |
| Validação | Hibernate Validator (Jakarta Bean Validation) |
| Build | Gradle 8 multi-module |
| Cobertura | JaCoCo |
| CI | GitHub Actions |

---

## Segurança

A autenticação é baseada em **JWT com assinatura RSA-256** (par de chaves assimétricas):

- O servidor assina tokens com a **chave privada** (`privateKey.pem`).
- A verificação é feita com a **chave pública** (`publicKey.pem`).
- O controle de acesso por perfil é implementado via `@RolesAllowed` — os papéis disponíveis são `CLIENTE` e `MECANICO`.
- Em CI, as chaves são geradas dinamicamente com `openssl` a cada execução do pipeline.

---

## Variáveis de Ambiente

| Variável | Descrição | Padrão (dev) |
|---|---|---|
| `QUARKUS_DATASOURCE_USERNAME` | Usuário do banco de dados | `servicetrack` |
| `QUARKUS_DATASOURCE_PASSWORD` | Senha do banco de dados | `servicetrack` |
| `QUARKUS_DATASOURCE_JDBC_URL` | JDBC URL do PostgreSQL | `jdbc:postgresql://localhost:5432/servicetrack` |

Em desenvolvimento (`%dev`), o banco utilizado é H2 em memória — não é necessário configurar variáveis de banco.

As chaves RSA devem estar disponíveis em:

```
software/service-track-api/_infrastructure/src/main/resources/keys/privateKey.pem
software/service-track-api/_infrastructure/src/main/resources/keys/publicKey.pem
```

---

## Como Executar

### Pré-requisitos

- JDK 21+
- Gradle (ou use o wrapper `./gradlew`)
- Docker e Docker Compose (opcional, para rodar via container)

### Executar localmente (modo dev)

```bash
cd software/service-track-api
./gradlew :_infrastructure:quarkusDev
```

O modo dev utiliza H2 em memória — nenhuma configuração de banco é necessária.  
O Swagger UI fica disponível em: `http://localhost:8080/q/swagger-ui`

### Executar com Docker

```bash
docker-compose up --build
```

A aplicação sobe conectada ao PostgreSQL configurado no `docker-compose.yml`.  
API disponível em: `http://localhost:8080`

### Build do artefato

```bash
cd software/service-track-api
./gradlew :_infrastructure:build
```

---

## Testes

O projeto possui testes em todas as camadas, com metas de cobertura aplicadas via JaCoCo:

| Módulo | Tipo | Cobertura mínima |
|---|---|---|
| `_domain` | Unitários (JUnit 5 + MockK) | 90% |
| `_application` | Unitários (JUnit 5 + MockK) | 80% |
| `_infrastructure` | Integração (`@QuarkusTest` + RestAssured) | 60% |

Para executar todos os testes:

```bash
cd software/service-track-api
./gradlew test
```

Para executar por módulo:

```bash
./gradlew :_domain:test
./gradlew :_application:test
./gradlew :_infrastructure:test
```

Os testes de integração sobem o contexto Quarkus completo com H2 em memória. O schema é recriado a cada execução (`drop-and-create`).

---

## Documentação

- **Contrato OpenAPI**: [`software/service-track-api/openapi.yaml`](software/service-track-api/openapi.yaml)
- **Swagger UI** (modo dev): `http://localhost:8080/q/swagger-ui`
- **SRS**: [`docs/srs.md`](docs/srs.md)
- **ADRs**: [`docs/adr/`](docs/adr/)
- **RFCs**: [`docs/rfc/`](docs/rfc/)

---

## Organização do Repositório

```
docs/
  adr/              → Architectural Decision Records
  rfc/              → Request for Comments (discussão técnica expandida)
  template/         → Templates para ADR e RFC
  srs.md            → Software Requirements Specification

software/
  service-track-api/
    _domain/        → Entidades, VOs, regras de negócio
    _application/   → Casos de uso, ports, DTOs
    _infrastructure/→ Adaptadores, persistência, REST, segurança
    openApi/        → Definições YAML do contrato de API

terraform/          → Infraestrutura como código (provisionamento cloud)
```

---

## Autor

Cláudio da Silva Araújo Filho — RM: 372729

---

## Licença

Projeto acadêmico — FIAP Pós-Graduação em Arquitetura de Software — 15SOAT
