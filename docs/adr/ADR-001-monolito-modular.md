# ADR – 001: Arquitetura Inicial do Sistema

## Data
18/04/2026

---

## Status

- Aceita

---

## Contexto

O sistema de gestão de ordens de serviço inicia como MVP com equipe reduzida. O domínio possui forte coesão entre entidades (Clientes, Veículos, Ordens de Serviço, Estoque), exige consistência transacional em operações críticas e prioriza entrega rápida com baixa complexidade operacional.

---

## Decisão

Adotar **Monolito Modular** como arquitetura inicial, estruturado com:

- Domain-Driven Design (DDD)
- Arquitetura Hexagonal
- Três módulos Gradle bem delimitados: `_domain`, `_application`, `_infrastructure`

A modularização interna garante separação de responsabilidades e preserva a capacidade de extração futura de serviços independentes.

---

## Consequências

### Positivas
- Simplicidade de desenvolvimento e deploy únicos
- Alta produtividade no MVP com equipe reduzida
- Consistência transacional (ACID) garantida sem overhead
- Menor custo operacional e facilidade de entendimento do sistema

### Negativas
- Escalabilidade horizontal limitada ao monolito completo
- Deploy único impacta todos os módulos simultaneamente
- Eventual necessidade de refatoração ao escalar por domínio

---

## Alternativas Consideradas

### Opção 1: Microsserviços
- Decomposição imediata em serviços autônomos por bounded context
- Prós: escala independente por domínio, deploy desacoplado, isolamento de falhas
- Contras: alta complexidade operacional, consistência eventual (Saga Pattern), maior custo de infraestrutura; prematuro para o estágio atual

---

## Referências

- [RFC-001 – Monolito Modular como Arquitetura Inicial](../rfc/RFC-001-monolito-modular.md)
- Domain-Driven Design — Eric Evans
- Arquitetura Hexagonal — Alistair Cockburn
- Strangler Pattern — Martin Fowler
