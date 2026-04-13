# ServiceTrack API

Sistema de gestão de ordens de serviço para oficina mecânica, desenvolvido como MVP com foco em organização operacional, rastreabilidade e eficiência no atendimento.

---

## Sobre o Projeto

Este projeto tem como objetivo resolver problemas comuns em oficinas mecânicas, como:

* Falta de controle no fluxo de ordens de serviço
* Erros na gestão de estoque
* Dificuldade de acompanhamento de status
* Perda de histórico de clientes e veículos

A solução consiste em uma API REST responsável por:

* Gestão de clientes e veículos
* Criação e acompanhamento de ordens de serviço
* Controle de peças e insumos
* Geração e aprovação de orçamentos

---

## Arquitetura

O sistema foi projetado utilizando:

* Monolito modular
* Domain-Driven Design (DDD)
* Arquitetura Hexagonal
* Clean Architecture

### Estrutura de módulos

```text
_domain         → Regras de negócio
_application    → Casos de uso
_infrastructure → Integrações externas (DB, API, etc.)
```

---

## Stack Tecnológica

* Kotlin
* Quarkus
* PostgreSQL
* Gradle (multi-module)
* OpenAPI (contract-first)
* Docker

---

## Segurança

* Autenticação baseada em JWT
* Assinatura com chave privada (RSA)
* Validação com chave pública
* Controle de acesso via RBAC

---

## Decisões Arquiteturais

As principais decisões do projeto estão documentadas em ADRs:

* [ADR-001 - Arquitetura Monolito Modular](docs/adr/ADR-001-monolito-modular.md)
* [ADR-002 - Banco PostgreSQL](docs/adr/ADR-002-postgresql.md)
* [ADR-003 - Kotlin](docs/adr/ADR-003-kotlin.md)
* [ADR-004 - Quarkus](docs/adr/ADR-004-quarkus.md)
* [ADR-005 - Autenticação JWT](docs/adr/ADR-005-autenticacao-jwt.md)

---

## Documentação

* SRS: [`docs/srs.md`](docs/srs.md)
* DDD (Diagramas): [`mvp-1/ddd`](mvp-1/ddd)
* OpenAPI: [`openapi.yaml`](software/service-track-api/openapi.yaml)

---

## Como executar o projeto

### Pré-requisitos

* Docker
* Docker Compose

---

### Subindo a aplicação

```bash
docker-compose up --build
```

---

### Acessando a API

* API: http://localhost:8080
* Swagger/OpenAPI: http://localhost:8080/q/swagger-ui

---

## Testes

O projeto possui:

* Testes unitários (_domain, _application)
* Testes de integração (_infrastructure)

Cobertura mínima (jacoco):

* Domain: 90%
* Application: 80%
* Infrastructure: 60%

---

## Evolução do Sistema

O sistema foi projetado para evolução incremental:

* Possível migração para microsserviços
* Introdução de mensageria
* Escalabilidade horizontal
* Externalização de autenticação (ex: Keycloak)

---

## Organização do Projeto

```text
docs/
  adr/
  mvp-1/
    ddd/
  srs.md

software/
  service-track-api/
    _domain/
    _application/
    _infrastructure/
```

---

## Autor

Cláudio da Silva Araújo Filho - RM: 372729

---

## Licença

Projeto acadêmico - FIAP Pós-Graduação em Arquitetura de Software - 15SOAT
