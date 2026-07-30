# ServiceTrack API

Backend para gestão de ordens de serviço de oficinas mecânicas. Responsável por controlar todo o ciclo de vida de uma OS — da abertura ao diagnóstico, orçamento, execução e entrega — com auditoria das operações de negócio.

**Este repositório contém apenas a aplicação.** Infraestrutura, banco e autenticação vivem em
repositórios próprios — ver [O sistema](#o-sistema).

---

## O sistema

O ServiceTrack é distribuído em quatro repositórios. Cada um tem um dono claro, e nenhum
provisiona o que pertence a outro.

| Repositório | É dono de |
|---|---|
| **service-track-api** *(este)* | domínio, casos de uso, API REST, migrations Flyway |
| [service-track-aws-iac](https://github.com/Claudio712005/service-track-aws-iac) | rede, EKS, API Gateway, contrato de exposição, manifestos Kubernetes, ArgoCD |
| [service-track-db-infra](https://github.com/Claudio712005/service-track-db-infra) | RDS PostgreSQL, orçamento de conexões, roles de runtime |
| [service-track-lambda](https://github.com/Claudio712005/service-track-lambda) | autenticação serverless por CPF e emissão de JWT |

**O que saiu desta aplicação:**

- **Infraestrutura e Kubernetes.** Terraform, manifestos e ArgoCD estão em
  `service-track-aws-iac`. Este repositório não provisiona nada.
- **Autenticação.** O login por CPF é da função serverless. Esta API **valida** os tokens
  emitidos por ela e assina apenas o token de decisão de orçamento do magic link
  ([ADR-014](docs/adr/ADR-014-aprovacao-orcamento-magic-link.md)). A troca de senha do usuário
  autenticado permanece aqui, em `PUT /usuarios/senha`.
- **Banco de dados.** A instância, as roles e o teto de conexões são de `service-track-db-infra`.
  O **schema** continua aqui, nas migrations Flyway.

Ordem de subida de um ambiente — rede → banco → stack — documentada no README do
`service-track-aws-iac`.

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
| Autenticação | JWT RS256 (SmallRye JWT) — **validação**; emissão fica na Lambda |
| Criptografia de senha | BCrypt |
| Build | Gradle Kotlin DSL (multi-module) |
| Containers | Docker + Docker Compose |
| Orquestração (prod) | Kubernetes — provisionado em `service-track-aws-iac` |
| CD | GitHub Actions publica a imagem no ECR e delega o deploy ao repositório de infraestrutura |
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
| [ADR-005](docs/adr/ADR-005-autenticacao-jwt.md) | JWT RS256 | Stateless, integrado via SmallRye JWT · emissão movida para a Lambda em `GLOBAL-ADR-004` |
| [ADR-019](docs/adr/ADR-019-observabilidade-opentelemetry.md) | Observabilidade OpenTelemetry | Vendor-neutral via OTLP; backend por configuração |
| [ADR-020](docs/adr/ADR-020-aplicacao-nao-e-dona-de-infraestrutura.md) | Aplicação sem infraestrutura | Uma única descrição da infra; CD delega o deploy |

As decisões de infraestrutura tomadas na Fase 2 — [ADR-015](docs/adr/ADR-015-kubernetes-eks.md)
(EKS), [ADR-016](docs/adr/ADR-016-terraform-iac.md) (Terraform),
[ADR-017](docs/adr/ADR-017-gitops-argocd.md) (GitOps) e
[ADR-018](docs/adr/ADR-018-bootstrap-scripts-operacionais.md) (bootstrap) — continuam válidas
como decisão, mas **a execução delas vive em `service-track-aws-iac`**. Os documentos ficam
aqui por serem o registro histórico de quando foram tomadas.

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
├── .github/workflows/     # ci, security, cd-app
├── docs/
│   ├── adr/               # Architecture Decision Records (001–019)
│   ├── rfc/               # Request for Comments (001–019)
│   ├── c4/                # Diagramas C4 (context, container, components, code)
│   ├── infra/             # Desenhos renderizados da Fase 2 (histórico)
│   ├── mvp-1/ mvp-2/      # Enunciados das fases + colinha do vídeo (mvp-2)
│   ├── template/          # Templates de ADR/RFC
│   └── srs.md             # Software Requirements Specification
└── software/
    └── service-track-api/
        ├── _domain/        # Regras de negócio puras
        ├── _application/   # Casos de uso, ports, DTOs
        ├── _infrastructure/ # REST, persistência, JWT, adapters
        ├── openApi/        # Especificações OpenAPI por recurso (contract-first)
        ├── openapi.yaml    # Spec agregada (input do OpenAPI Generator)
        ├── observability/  # Configs do stack local: otel-collector, prometheus, grafana
        ├── scripts/        # postgres-init (roles), security-scan, convert-to-sarif
        ├── service-track.postman_collection.json  # Collection das APIs
        ├── docker-compose.yaml
        ├── Dockerfile
        └── build.gradle.kts
```

---

## CI e CD

### CI — `.github/workflows/ci.yml`

Executa em pushes para `main`, `develop` e `fase-*`.

**Jobs (encadeados):**

| Job | O que faz |
|---|---|
| Domain Coverage | `./gradlew :_domain:test :_domain:jacocoTestReport` |
| Application Coverage | `./gradlew :_application:test :_application:jacocoTestReport` |
| Infrastructure Coverage | Gera chaves JWT temporárias via OpenSSL, executa `./gradlew :_infrastructure:test :_infrastructure:jacocoTestReport` |
| Sonar Analysis | Agrega os três relatórios e envia para SonarCloud |

### CD — `.github/workflows/cd-app.yml`

Este repositório **publica a imagem e delega o deploy**. Não aplica Terraform nem toca no
cluster.

```
push na main ──► build ──► push no ECR servicetrack-<env>-app (tag = commit SHA)
                              │
                              ├─ portão: falha se o scan do ECR apontar CRITICAL
                              │
                              └─ repository_dispatch (image-published)
                                        │
                                        ▼
                   service-track-aws-iac reescreve a tag do overlay
                                        │
                                        ▼
                            ArgoCD sincroniza o cluster
```

| Gatilho | Ambiente |
|---|---|
| push na `main` | `hml`, automático |
| **Run workflow** | `hml` ou `prd`; informando `image_tag`, promove uma imagem já publicada sem reconstruir |

Segredos necessários: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` por
environment, e `IAC_REPO_TOKEN` — um token fino com `contents: write` apenas no repositório de
infraestrutura. Se vazar, o dano máximo é um commit de bump, revertível.

O portão de vulnerabilidade é responsabilidade desta esteira, por decisão registrada no
repositório de infraestrutura: ele só recebe uma tag já aprovada.

---

## Segurança

Pipeline em `.github/workflows/security.yml`. Executa nos mesmos branches do CI.

**SAST com Semgrep:**
- Analisa todo o código com regras `auto`
- Gera relatórios JSON e SARIF
- **Bloqueia o pipeline** se houver findings Critical/High
- SARIF enviado ao GitHub Code Scanning (Security tab)

---

## Observabilidade

Instrumentação **vendor-neutral** com OpenTelemetry, exportando traces e métricas por OTLP.
A aplicação não conhece o backend: quem responde no endpoint OTLP é uma decisão de ambiente
([ADR-019](docs/adr/ADR-019-observabilidade-opentelemetry.md)).

### Local: Grafana ou Datadog, mesma aplicação

O compose sobe **um dos dois**, e a aplicação não muda de configuração. Os dois backends
respondem pelo mesmo nome de rede, `coletor-otlp`:

```bash
docker compose --profile grafana up --build     # padrão para desenvolvimento
docker compose --profile datadog up --build     # exige DD_API_KEY no .env
```

| Profile | O que sobe | Onde ver |
|---|---|---|
| `grafana` | Collector, Jaeger, Prometheus, **Loki**, **Promtail**, Grafana | Grafana `:3001` (traces, métricas e logs) · Jaeger `:16686` · Prometheus `:9090` |
| `datadog` | Datadog Agent com OTLP, APM e coleta de logs | app.datadoghq.com |

Traces e métricas chegam por OTLP. **Logs chegam por outro caminho:** a aplicação escreve JSON
no stdout e o Promtail lê pelo socket do Docker, empurrando para o Loki — o mesmo princípio que
o agente do Datadog usa na nuvem. Exportar log por OTLP exigiria Quarkus 3.16+.

No Grafana, o campo `traceId` do log é clicável e leva ao trace no Jaeger.

`grafana` é o padrão para teste local: roda **100% offline**, sem conta, sem chave, sem SaaS.
`datadog` existe para reproduzir localmente o que roda em `hml` e `prd`.

> Subir os dois profiles ao mesmo tempo faz o alias `coletor-otlp` ficar ambíguo. Suba um de
> cada vez.

### Logs estruturados e rastreabilidade dos casos de uso

No perfil `prod` os logs saem em **JSON**, com `traceId` e `spanId` no MDC. Em modo de
desenvolvimento seguem legíveis.

Todo caso de uso da camada de aplicação é observado por um proxy dinâmico (`UseCaseProxy`),
que emite três sinais sem que o caso de uso saiba disso:

| Sinal | Nome |
|---|---|
| Span | código do caso de uso, por exemplo `OS_CRIAR` |
| Log | `use_case`, `entidade`, `duracao_ms`, `erro_codigo`, `erro_tipo` + campos marcados |
| Métrica | `servicetrack.usecase.duracao` e `servicetrack.usecase.execucoes`, com tags `use_case`, `entidade` e `resultado` |

O que aparece do payload é **decidido por anotação, e o padrão é não aparecer**: só campos
marcados com `@Rastreavel` são logados, e `@Mascarado` revela apenas os últimos dígitos. Senha
não tem anotação nenhuma — não aparece nem mascarada.

Mensagem de exceção só é logada quando a exceção vem dos pacotes da aplicação ou do domínio.
Erro de terceiro registra apenas o tipo, porque a mensagem pode carregar dado do usuário.

### Em nuvem

Datadog em `hml` e `prd`, provisionado por Terraform no
[service-track-aws-iac](https://github.com/Claudio712005/service-track-aws-iac). A aplicação
envia OTLP para o agente do **próprio node**, via `status.hostIP` — nenhuma configuração da
aplicação muda entre local e nuvem, apenas o endereço.

Variáveis que o ambiente fornece:

| Variável | De onde vem |
|---|---|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `status.hostIP` do node, no Deployment |
| `OTEL_ENVIRONMENT` | ConfigMap do overlay (`local`, `hml`, `prd`) |
| `OTEL_SERVICE_NAME` | Deployment |

---

## Cobertura de código

Medida por módulo com JaCoCo e consolidada no SonarCloud. Exclusões: DTOs, entities JPA, classes de configuração e código gerado pelo OpenAPI Generator.

---

## Roadmap / Evoluções futuras

| Item | Status |
|---|---|
| Notificações ao cliente (e-mail) | **Implementado** — [ADR-009](docs/adr/ADR-009-notificacoes-email.md), [ADR-014](docs/adr/ADR-014-aprovacao-orcamento-magic-link.md) |
| Observabilidade OpenTelemetry | **Implementado** — [ADR-019](docs/adr/ADR-019-observabilidade-opentelemetry.md) |
| Logs estruturados em JSON com correlação | Pendente |
| Migração para microsserviços | Possível evolução pós-validação do monólito |

Itens de infraestrutura — Terraform, Kubernetes com HPA, GitOps e secrets — são acompanhados
nos repositórios donos: [service-track-aws-iac](https://github.com/Claudio712005/service-track-aws-iac)
e [service-track-db-infra](https://github.com/Claudio712005/service-track-db-infra).
