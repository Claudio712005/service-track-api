# ServiceTrack API

Backend para gestão de ordens de serviço de oficinas mecânicas. Responsável por controlar todo o ciclo de vida de uma OS — da abertura ao diagnóstico, orçamento, execução e entrega — com rastreabilidade completa por auditoria.

---

## Contexto de negócio

Uma oficina mecânica precisa registrar e acompanhar cada atendimento. O sistema suporta:

- Abertura de OS por cliente (dados de cliente e veículo) — nasce em `RECEBIDA`
- Abertura completa de OS pelo mecânico (já com serviços e insumos diagnosticados) — nasce em `EM_DIAGNOSTICO`
- Diagnóstico pelo mecânico (associação de serviços e insumos)
- Geração de orçamento com custo de mão de obra e insumos
- Aprovação ou reprovação do orçamento pelo cliente (no app ou por link/botão no e-mail — magic link)
- Execução dos serviços com registro por mecânico responsável
- Finalização e entrega do veículo

### Abertura de OS: dois caminhos

| Rota | Ator | Payload | Status inicial |
|---|---|---|---|
| `POST /ordem-servico` | Cliente (ou mecânico) | motivo, cliente, mecânico, veículo | `RECEBIDA` |
| `POST /ordem-servico/completa` | Mecânico | motivo, cliente, veículo, **serviços + insumos** | `EM_DIAGNOSTICO` |

O cliente não conhece serviços e peças ao abrir a OS — quem diagnostica é o mecânico. Por isso a
abertura completa é exclusiva do mecânico: ele abre a OS já com os itens diagnosticados, o mecânico
vinculado é o próprio solicitante autenticado e a OS entra direto em diagnóstico, pronta para a
geração do orçamento (`POST /ordem-servico/{id}/orcamento`).

---

## Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.0.21 + JVM 21 |
| Framework | Quarkus 3.15.1 |
| Persistência (prod) | PostgreSQL 16 |
| Persistência (dev/test) | H2 in-memory |
| ORM | Hibernate ORM (via Quarkus) |
| Autenticação | JWT RS256 (SmallRye JWT) |
| Criptografia de senha | BCrypt |
| Build | Gradle Kotlin DSL (multi-module) |
| Containers | Docker + Docker Compose |
| Qualidade | JaCoCo + SonarCloud |
| Segurança (SAST) | Semgrep |
| CI | GitHub Actions |

---

## Arquitetura

O projeto é um **monólito modular** estruturado em três módulos Gradle alinhados com Hexagonal Architecture e DDD:

```
_domain          ← regras de negócio puras (sem dependência de framework)
_application     ← orquestração de casos de uso, ports, DTOs, services
_infrastructure  ← REST, persistência, JWT, interceptors, adapters
```

A regra de dependência segue a direção:

```
infrastructure → application → domain
```

`_domain` não conhece `_application` nem `_infrastructure`. `_application` não conhece `_infrastructure`. A inversão de dependência é feita via interfaces (ports) definidas em `_application` e implementadas em `_infrastructure`.

Para detalhes de cada camada, veja:
- [_domain/README.md](software/service-track-api/_domain/README.md)
- [_application/README.md](software/service-track-api/_application/README.md)
- [_infrastructure/README.md](software/service-track-api/_infrastructure/README.md)

---

## Principais decisões arquiteturais

| ADR | Decisão | Razão resumida |
|---|---|---|
| [ADR-001](docs/adr/ADR-001-monolito-modular.md) | Monólito Modular | Menor complexidade operacional no MVP |
| [ADR-002](docs/adr/ADR-002-postgresql.md) | PostgreSQL | Banco relacional robusto para dados transacionais |
| [ADR-003](docs/adr/ADR-003-kotlin.md) | Kotlin | Expressividade, null safety, value classes |
| [ADR-004](docs/adr/ADR-004-quarkus.md) | Quarkus | Startup rápido, suporte nativo a CDI/MicroProfile |
| [ADR-005](docs/adr/ADR-005-autenticacao-jwt.md) | JWT RS256 | Stateless, integrado via SmallRye JWT |

---

## Como rodar o projeto

### Pré-requisitos

| Ferramenta | Versão mínima | Observação |
|---|---|---|
| Docker Engine / Docker Desktop | 24+ | BuildKit habilitado por padrão |
| Docker Compose | v2 (`docker compose`) | Integrado ao Docker Desktop |

> **Apple Silicon (M1/M2/M3):** o build é nativo em ARM64. Para gerar uma imagem compatível com servidores Linux AMD64, use `docker buildx build --platform linux/amd64 -t servicetrack-api .` antes do `docker compose up`.

### Variáveis de ambiente

```bash
cd software/service-track-api
cp .env.example .env
```

Edite `.env` com os valores desejados. O arquivo **nunca deve ser commitado** (já coberto pelo `.gitignore`).

As chaves JWT devem estar em `_infrastructure/src/main/resources/keys/`:

```bash
openssl genrsa -out privateKey.pem 4096
openssl rsa -in privateKey.pem -pubout -out publicKey.pem
```

### Subindo com Docker Compose

```bash
cd software/service-track-api
docker compose up --build
```

O Compose aguarda o Postgres passar no healthcheck antes de iniciar a API — a primeira subida pode levar alguns segundos extras.

| Serviço | URL |
|---|---|
| API | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |
| Swagger UI | `http://localhost:8080/q/swagger-ui` |

#### Rebuild sem cache (quando necessário)

```bash
docker compose build --no-cache
docker compose up
```

### Modo dev (H2 in-memory)

```bash
cd software/service-track-api
./gradlew :_infrastructure:quarkusDev
```

Console H2 disponível em `http://localhost:8080/h2-console`.

> **Windows:** certifique-se de usar o terminal WSL2 ou Git Bash. O `gradlew` requer line endings LF — o `.gitattributes` na raiz garante isso automaticamente ao clonar.

---

## Como rodar os testes

```bash
cd software/service-track-api

# Testes unitários de domínio (sem framework)
./gradlew :_domain:test

# Testes unitários de application (MockK)
./gradlew :_application:test

# Testes de integração (QuarkusTest + H2) — exige chaves JWT em _infrastructure/src/test/resources/keys/
./gradlew :_infrastructure:test
```

Geração de relatórios JaCoCo por módulo:

```bash
./gradlew :_domain:jacocoTestReport
./gradlew :_application:jacocoTestReport
./gradlew :_infrastructure:jacocoTestReport
# Saída: <módulo>/build/reports/jacoco/test/jacocoTestReport.xml
```

---

## OpenAPI / Swagger UI

O projeto adota abordagem **contract-first**. Os contratos estão em `software/service-track-api/openApi/`.

Com a aplicação rodando:

```
http://localhost:8080/q/swagger-ui
```

---

## Estrutura de pastas

```
ServiceTrack-API/
├── docs/
│   ├── adr/               # Architecture Decision Records
│   ├── rfc/               # Request for Comments
│   ├── c4/                # Diagramas C4 (context, container, components, code)
│   ├── mvp-1/             # Domain Storytelling e Event Storming
│   └── srs.md             # Software Requirements Specification
└── software/
    └── service-track-api/
        ├── _domain/       # Regras de negócio puras
        ├── _application/  # Casos de uso, ports, DTOs
        ├── _infrastructure/ # REST, persistência, JWT, adapters
        ├── openApi/       # Especificações OpenAPI (contract-first)
        ├── docker-compose.yaml
        ├── Dockerfile
        └── build.gradle.kts
```

---

## CI

Pipeline em `.github/workflows/ci.yml`. Executa em pushes para `main`, `develop` e `fase-*`.

**Jobs (encadeados):**

| Job | O que faz |
|---|---|
| Domain Coverage | `./gradlew :_domain:test :_domain:jacocoTestReport` |
| Application Coverage | `./gradlew :_application:test :_application:jacocoTestReport` |
| Infrastructure Coverage | Gera chaves JWT temporárias via OpenSSL, executa `./gradlew :_infrastructure:test :_infrastructure:jacocoTestReport` |
| Sonar Analysis | Agrega os três relatórios e envia para SonarCloud |

---

## Segurança

Pipeline em `.github/workflows/security.yml`. Executa nos mesmos branches do CI.

**SAST com Semgrep:**
- Analisa todo o código com regras `auto`
- Gera relatórios JSON e SARIF
- **Bloqueia o pipeline** se houver findings Critical/High
- SARIF enviado ao GitHub Code Scanning (Security tab)

---

## Cobertura de código

Medida por módulo com JaCoCo e consolidada no SonarCloud. Exclusões: DTOs, entities JPA, classes de configuração e código gerado pelo OpenAPI Generator.

---

## Roadmap / Evoluções futuras

| Item | Status |
|---|---|
| Infraestrutura como código (Terraform) | **Não implementado** |
| Pipeline de CD / deploy automatizado | **Não implementado** |
| Notificações ao cliente (e-mail/SMS) | Possível evolução |
| Migração para microsserviços | Possível evolução pós-validação do monólito |
