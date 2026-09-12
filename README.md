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
| [ADR-021](docs/adr/ADR-021-datadog-backend-unico.md) | Datadog como backend único | Um caminho só de observabilidade, do local à produção |
| [ADR-022](docs/adr/ADR-022-tracer-datadog-no-ambiente-local.md) | Tracer do Datadog local | Traces pelo tracer, métricas por OTLP; auth instrumentado sem código |

### Decisões que saíram deste repositório

As decisões de infraestrutura tomadas na Fase 2 foram transferidas para
`service-track-aws-iac`, que é quem as executa. O conteúdo foi preservado; mudou a numeração,
para não colidir com os ADRs que já existiam lá.

| Era aqui | Passou a ser | Assunto |
|---|---|---|
| `API-ADR-015` / `API-RFC-015` | `IAC-ADR-019` / `IAC-RFC-002` | Kubernetes no EKS |
| `API-ADR-016` / `API-RFC-016` | `IAC-ADR-020` / `IAC-RFC-003` | Terraform |
| `API-ADR-017` / `API-RFC-017` | `IAC-ADR-021` / `IAC-RFC-004` | GitOps com ArgoCD |
| `API-ADR-018` / `API-RFC-018` | `IAC-ADR-022` / `IAC-RFC-005` | Bootstrap de segredos |

A numeração local **não foi reaproveitada**: 015 a 018 seguem vagos, para que referências
antigas — inclusive as dos relatórios em PDF das Fases 1 e 2 — continuem apontando para o
lugar certo pela tabela acima.

Os desenhos de rede, deployment e CI/CD da Fase 2 estão em
[`docs/mvp-2/infra-fase-2/`](docs/mvp-2/infra-fase-2/). Descrevem o cluster `servicetrack-dev`,
que não existe mais — valem como registro da entrega, não como referência. Os diagramas atuais
estão em `service-track-aws-iac/docs/diagramas/`.

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

> **O par é compartilhado com o serviço de autenticação.** Quem emite o token de login é a
> Lambda; quem o verifica é esta aplicação — que também assina com a mesma chave privada, nos
> tokens de decisão de orçamento. Gerar um par novo aqui exige copiá-lo para
> `service-track-lambda/src/main/resources/`, senão todo login local termina em 401.
> Ver `GLOBAL-RFC-007`.

### Construindo a imagem de autenticação

O login não está mais nesta aplicação: ele é um serviço à parte, em
[service-track-lambda](https://github.com/Claudio712005/service-track-lambda). O Compose
consome a imagem dele por nome, e ela precisa existir antes da primeira subida:

```bash
cd ../../../service-track-lambda/service-track-lambda
docker build -f Dockerfile.local -t servicetrack-auth:local .
```

O build acontece dentro da imagem — não exige JDK na máquina. Refaça sempre que o código de
autenticação mudar. Para apontar para outra imagem, use `AUTH_IMAGE` no `.env`.

### Testando um ambiente de nuvem pelo Postman

URL e chave de API mudam a cada recriação, então a coleção versionada não os carrega.
Gere uma cópia pronta para o ambiente:

```bash
cd software/service-track-api
./scripts/gerar-collection.sh hml          # colecao com url e chave embutidas
```

Importe `servicetrack-hml.postman_collection.json` e use — não precisa selecionar
environment. O arquivo contém a chave e é ignorado por git.

Para manter a coleção versionada e trocar só o ambiente, existe a alternativa por
environment:

```bash
./scripts/gerar-env-postman.sh hml
```

Nesse caso é obrigatório **selecionar o environment** no canto superior direito do Postman —
sem isso a chave fica vazia e o gateway devolve `403`.

### Subindo com Docker Compose

```bash
cd software/service-track-api
docker compose up --build
```

A ordem de subida é encadeada por healthcheck: Postgres → API → autenticação. A API precisa
estar saudável antes do serviço de autenticação porque quem cria as tabelas `usuarios` e
`usuario_roles` é o Flyway daqui. A primeira subida leva alguns segundos extras.

| Serviço | URL |
|---|---|
| API | `http://localhost:8080` |
| Autenticação | `http://localhost:8081` |
| PostgreSQL | `localhost:5432` |
| Swagger UI | `http://localhost:8080/q/swagger-ui` |

#### Obtendo um token

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/autenticacao \
  -H 'Content-Type: application/json' \
  -d '{"cpf":"13646633093","senha":"<senha do seed>"}' | jq -r .token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/clientes
```

Os CPFs disponíveis vêm do seed em `V2__seed_data.sql`.

#### Rebuild sem cache (quando necessário)

```bash
docker compose build --no-cache
docker compose up
```

#### Consumo de disco

As imagens de terceiros têm versão fixa no `docker-compose.yaml` (`postgres:16.15-alpine`,
`gcr.io/datadoghq/agent:7.82.3`). Tag flutuante baixa uma imagem inteira a cada release
upstream e deixa a anterior pendurada — o agente passa de 1 GB, então acumula rápido.
Atualizar é deliberado: troque a versão no compose.

As duas imagens próprias compartilham a base `eclipse-temurin:21-jre-alpine`, então a segunda
custa só as camadas da aplicação.

O que mais ocupa espaço não são as imagens, e sim o **cache de build**: os dois `Dockerfile`
usam `--mount=type=cache` para o diretório do Gradle, que cresce sem teto. Vendo e limpando:

```bash
docker system df                 # onde está o espaço
docker builder prune             # cache de build — costuma ser o maior
docker image prune               # imagens penduradas, sobra de --build repetido
docker system prune -a --volumes # radical: remove tudo que não está em uso, inclusive o banco
```

O último apaga o volume `postgres_data`. Perder esse volume é barato — o Flyway reaplica
`V1..V3` com o seed na próxima subida — mas é perda de dado, então não rode por reflexo.

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
│   ├── adr/               # Architecture Decision Records (001–014, 019, 020)
│   ├── rfc/               # Request for Comments (001–014, 019)
│   ├── c4/                # Diagramas C4 (context, container, components, code)
│   ├── mvp-1/ mvp-2/      # Enunciados das fases + colinha do vídeo (mvp-2)
│   │   └── infra-fase-2/  # Desenhos de rede/deploy da Fase 2 (histórico)
│   ├── template/          # Templates de ADR/RFC
│   └── srs.md             # Software Requirements Specification
└── software/
    └── service-track-api/
        ├── _domain/        # Regras de negócio puras
        ├── _application/   # Casos de uso, ports, DTOs
        ├── _infrastructure/ # REST, persistência, JWT, adapters
        ├── openApi/        # Especificações OpenAPI por recurso (contract-first)
        ├── openapi.yaml    # Spec agregada (input do OpenAPI Generator)
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
environment, e `OPS_TOKEN` — a credencial única de integração entre repositórios, com `contents: write` e
`actions: read` no repositório de
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

### Local: Datadog, o mesmo backend de hml e prd

```bash
docker compose up --build
```

O agente do Datadog sobe junto e recebe OTLP pelo alias de rede `coletor-otlp`. **Exige
`DD_API_KEY` no `.env`** — sem chave o agente não sobe, e a aplicação passa a repetir
`UnknownHostException: coletor-otlp` a cada 15 segundos ao tentar exportar. Não é defeito de
configuração; é a chave faltando.

Traces e métricas chegam por OTLP. **Logs chegam por outro caminho:** a aplicação escreve JSON
no stdout e o agente lê pelo socket do Docker — o mesmo mecanismo que ele usa no cluster.
Exportar log por OTLP exigiria Quarkus 3.16+.

Até a Fase 3 havia um segundo caminho local, com Grafana, Prometheus, Loki e Jaeger atrás de
um profile. Foi removido: manter dois backends dobrava a manutenção e fazia o ambiente local
deixar de ser evidência do que roda em produção. Custo assumido — não há mais observabilidade
local 100% offline. Decisão em `GLOBAL-RFC-007` e [ADR-021](docs/adr/ADR-021-datadog-backend-unico.md).

A instrumentação **não mudou**: continua OpenTelemetry puro, exportando por OTLP. A aplicação
não conhece o fornecedor — o alias `coletor-otlp` existe justamente para manter isso
([ADR-019](docs/adr/ADR-019-observabilidade-opentelemetry.md)).

#### O que abrir no Datadog

`app.datadoghq.com`, filtrando por `env:local`:

| Onde | O que mostra |
|---|---|
| APM → Services | `service-track-api` e `service-track-auth`, latência e taxa de erro por rota |
| Metrics Explorer | `servicetrack.usecase.duracao` e `servicetrack.usecase.execucoes`, por `use_case`, `entidade` e `resultado` |
| Logs | `service:service-track-api env:local` — linhas dos casos de uso, com `traceId` clicável para o trace |

Se não aparecer nada, o motivo quase sempre é um destes: `DD_API_KEY` vazia ou de outro site
(confira `DD_SITE`), ainda não passaram os 15 segundos do primeiro ciclo de exportação de
métricas, ou não houve tráfego — o agendador de notificações gera dado sozinho depois de ~30 s.

Diagnóstico do agente:

```bash
docker exec dd-agent agent status
```

Na seção `OTLP`, `Collector status` precisa estar `Running`. Se estiver `Closed`, o agente
abortou o pipeline no boot e as portas 4317/4318 não sobem — a aplicação passa a exportar
contra porta fechada e registra `Connection refused: coletor-otlp`. A causa conhecida são
mounts de `/proc` e `/sys/fs/cgroup` no container do agente, que quebram a telemetria interna
do collector (`failed to register process metrics`).

Os dashboards e monitores de `hml` e `prd` são provisionados por Terraform e filtram
`env:hml` / `env:prd`. O ambiente local não aparece neles, por desenho.

#### Como os sinais chegam

| Sinal | Caminho | Destino |
|---|---|---|
| Traces | `dd-java-agent.jar` injetado como `-javaagent` | agente `:8126` |
| Métricas de negócio | Micrometer, exportador OTLP | agente `:4318` |
| Logs | stdout em JSON, lidos pelo socket do Docker | agente |

O tracer está nas duas imagens — aplicação e autenticação — **desligado por padrão**
(`DD_TRACE_ENABLED=false`). Só o compose liga. Rodar a imagem fora do compose se comporta
como antes, sem tentar exportar nada.

É o tracer que torna o **serviço de autenticação visível no APM**: ele instrumenta JAX-RS,
JDBC e cliente HTTP sem alterar código. Antes disso o fluxo de login não gerava trace nenhum.
Ver [ADR-022](docs/adr/ADR-022-tracer-datadog-no-ambiente-local.md).

> **Local e nuvem divergem no caminho de traces.** `hml` e `prd` continuam exportando por
> OTLP; o local usa o tracer. Métricas e logs seguem idênticos nos dois. Consequência prática:
> os nomes de métrica derivadas de trace diferem — os monitores do Terraform consultam
> `trace.http.server.request`, que é o nome gerado pela conversão OTLP e **não** aparece
> localmente.

Verificando que o tracer subiu:

```bash
docker logs servicetrack-api 2>&1 | grep "DATADOG TRACER CONFIGURATION"
```

Procure `"agent_error":false` e o `"service"` correto. Quantos traces chegaram:

```bash
docker exec datadog-agent agent status | grep -A2 "Traces received"
```


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
