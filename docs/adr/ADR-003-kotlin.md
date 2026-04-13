# ADR-003: Escolha da Linguagem de Programação - Kotlin

## Status

Aprovada

---

## Context

O sistema de gestão de ordens de serviço será desenvolvido como um back-end monolítico inicialmente, utilizando arquitetura baseada em Domain-Driven Design (DDD), com forte ênfase em:

* Modelagem de domínio rica
* Regras de negócio bem definidas
* Alta manutenibilidade
* Redução de erros em produção
* Integração com o ecossistema Java (frameworks, bibliotecas)

A linguagem escolhida precisa:

* Ter alta produtividade
* Reduzir boilerplate
* Garantir segurança de tipos
* Integrar facilmente com frameworks modernos
* Facilitar evolução futura do sistema

Foram consideradas as seguintes opções:

* Kotlin
* Java

---

## Decision

Será utilizada a linguagem Kotlin como base para o desenvolvimento do sistema.

---

## Justificativa

A escolha do Kotlin foi baseada nos seguintes fatores comparativos:

---

### 1. Redução de Boilerplate

**Kotlin vs Java:**

* Kotlin reduz drasticamente código repetitivo
* Recursos como:

    * data classes
    * properties automáticas
    * default values
* Em Java, seria necessário uso de:

    * getters/setters
    * builders
    * anotações adicionais (ex: Lombok)

**Impacto:**

* Código mais limpo
* Maior produtividade
* Menor chance de erro humano

---

### 2. Segurança contra NullPointerException

**Kotlin vs Java:**

* Kotlin possui sistema de null safety nativo
* Diferencia tipos nullable e non-nullable
* Java permite null livremente, aumentando risco de falhas em runtime

**Impacto:**

* Redução de erros críticos
* Maior confiabilidade em produção

---

### 3. Melhor suporte a Domain-Driven Design

**Kotlin vs Java:**

* Kotlin facilita criação de modelos ricos e imutáveis
* Suporte natural a:

    * classes imutáveis
    * value objects
    * sealed classes
* Java exige mais verbosidade para alcançar o mesmo nível

**Impacto:**

* Melhor representação do domínio
* Código mais expressivo

---

### 4. Programação mais concisa e expressiva

**Kotlin vs Java:**

* Sintaxe mais enxuta e moderna
* Suporte a:

    * funções de extensão
    * lambdas mais simples
    * DSLs

**Impacto:**

* Código mais legível
* Menor esforço de manutenção

---

### 5. Interoperabilidade com Java

**Kotlin vs Java:**

* Kotlin é 100% interoperável com Java
* Permite uso de:

    * bibliotecas existentes
    * frameworks (ex: Quarkus, Spring)

**Impacto:**

* Sem lock-in tecnológico
* Aproveitamento do ecossistema consolidado

---

### 6. Produtividade e velocidade de desenvolvimento

**Kotlin vs Java:**

* Menos código → menos tempo de desenvolvimento
* Melhor suporte a práticas modernas

**Impacto:**

* Entrega mais rápida do MVP
* Redução de custo de desenvolvimento

---

## Alternatives Considered

### Java

**Prós:**

* Linguagem consolidada
* Grande comunidade
* Ampla documentação
* Estável e madura

**Contras:**

* Alto nível de boilerplate
* Maior propensão a erros com null
* Código mais verboso
* Menor produtividade comparado ao Kotlin
* Necessidade de bibliotecas auxiliares (ex: Lombok)

---

## Consequences

### Positivas

* Código mais limpo e conciso
* Redução de erros comuns (especialmente null)
* Melhor modelagem de domínio (DDD)
* Maior produtividade da equipe
* Integração total com ecossistema Java

---

### Negativas

* Curva de aprendizado inicial (caso equipe não conheça Kotlin)
* Menor número de desenvolvedores experientes comparado ao Java
* Possível necessidade de adaptação em algumas bibliotecas

---

## Scope

Esta decisão se aplica a:

* Todo o desenvolvimento do back-end
* Camadas de domínio, aplicação e infraestrutura

---

## Future Considerations

* Possível adoção de recursos avançados da linguagem:

    * Coroutines (programação assíncrona)
    * DSLs para regras de negócio
* Avaliação contínua de performance e produtividade
* Treinamento da equipe em Kotlin

---

## References

* Documentação oficial do Kotlin
* Boas práticas de desenvolvimento com Kotlin
* Princípios de Domain-Driven Design (DDD)
