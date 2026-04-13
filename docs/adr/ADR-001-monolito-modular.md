# ADR-001: Arquitetura do Sistema - Monolito Modular

## Status

Aprovada

---

## Context

O sistema de gestão de ordens de serviço será desenvolvido como um MVP, com foco em:

* Entrega rápida de valor
* Validação do domínio de negócio
* Redução de complexidade operacional inicial
* Facilidade de desenvolvimento e deploy

O sistema apresenta características como:

* Domínio bem definido (clientes, veículos, ordens de serviço, estoque)
* Forte coesão entre módulos
* Necessidade de consistência transacional
* Equipe reduzida
* Baixa necessidade inicial de escalabilidade independente

Foram consideradas as seguintes abordagens arquiteturais:

* Monolito modular
* Microsserviços

---

## Decision

Será adotada uma arquitetura de **monolito modular como abordagem inicial**.

O sistema será estruturado em módulos bem definidos, seguindo princípios de:

* Domain-Driven Design (DDD)
* Arquitetura em camadas
* Arquitetura hexagonal

Separação lógica:

* `_domain`
* `_application`
* `_infrastructure`

---

## Justificativa

### 1. Simplicidade operacional

**Monolito vs Microsserviços:**

* Monolito possui:

    * Deploy único
    * Menor overhead operacional
* Microsserviços exigem:

    * Orquestração
    * Observabilidade
    * Comunicação distribuída

**Impacto:**

* Redução de complexidade no MVP
* Maior velocidade de entrega

---

### 2. Consistência transacional

O sistema exige operações consistentes:

* Atualização de estoque
* Mudança de status da OS
* Aprovação de orçamento

**Monolito:**

* Transações locais simples (ACID)

**Microsserviços:**

* Necessidade de:

    * Saga pattern
    * Eventual consistency

**Impacto:**

* Menor risco de inconsistência
* Implementação mais simples

---

### 3. Forte acoplamento natural do domínio

O domínio possui relações diretas:

* Cliente ↔ Veículo
* Veículo ↔ Ordem de Serviço
* Ordem de Serviço ↔ Itens ↔ Estoque

**Impacto:**

* Separação prematura em serviços geraria:

    * Alto acoplamento distribuído
    * Complexidade desnecessária

---

### 4. Custo e maturidade técnica

Microsserviços exigem:

* Infraestrutura mais robusta
* Monitoramento distribuído
* Estratégias de resiliência

**Impacto:**

* Overhead desnecessário para o momento do sistema

---

### 5. Preparação para evolução

A arquitetura será organizada de forma modular, permitindo:

* Separação clara de domínios
* Baixo acoplamento entre módulos
* Identificação de bounded contexts

---

## Alternatives Considered

### Microsserviços

**Prós:**

* Escalabilidade independente
* Deploy desacoplado
* Isolamento de falhas

**Contras:**

* Alta complexidade operacional
* Comunicação distribuída
* Consistência eventual
* Overhead de infraestrutura

---

## Consequences

### Positivas

* Simplicidade de desenvolvimento e deploy
* Alta produtividade
* Consistência transacional garantida
* Menor custo operacional
* Facilidade de entendimento do sistema

---

### Negativas

* Escalabilidade limitada inicialmente
* Deploy único pode impactar todo o sistema
* Necessidade futura de refatoração para escalar

---

## Evolution Strategy

A arquitetura foi definida considerando evolução incremental.

O monolito modular permitirá:

* Isolamento lógico de domínios
* Identificação de bounded contexts
* Preparação para extração de serviços

### Critérios para migração para microsserviços:

* Aumento significativo de carga
* Necessidade de escalabilidade independente por domínio
* Times atuando de forma independente por módulo
* Gargalos de deploy
* Crescimento da complexidade do domínio

### Estratégia de migração:

* Aplicação do **Strangler Pattern**
* Extração gradual de módulos críticos
* Introdução de comunicação assíncrona (eventos)
* Separação de bancos por serviço (quando necessário)

---

## Scope

Esta decisão se aplica a:

* Toda a arquitetura do sistema no MVP
* Estrutura modular do backend

---

## References

* Domain-Driven Design (DDD)
* Arquitetura Hexagonal
* Strangler Pattern
* Práticas de arquitetura evolutiva
