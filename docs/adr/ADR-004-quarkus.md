# ADR-004: Escolha do Framework Backend - Quarkus

## Status

Aprovada

---

## Context

O sistema de gestão de ordens de serviço será desenvolvido como um back-end monolítico, com arquitetura baseada em Domain-Driven Design (DDD), seguindo princípios de:

* Arquitetura em camadas
* Clean Architecture
* Arquitetura hexagonal
* APIs REST

O sistema será inicialmente um MVP, com possibilidade de evolução futura para:

* Escalabilidade horizontal
* Integração com outros sistemas
* Possível decomposição em microserviços

O framework escolhido precisa:

* Ter alta performance
* Baixo tempo de inicialização
* Baixo consumo de memória
* Boa integração com containers (Docker)
* Facilidade de desenvolvimento
* Integração com o ecossistema Java/Kotlin

Foram consideradas as seguintes opções:

* Quarkus
* Spring Boot

---

## Decision

Será utilizado o Quarkus como framework principal para o desenvolvimento do back-end.

---

## Justificativa

A escolha do Quarkus foi baseada nos seguintes fatores comparativos e aderência ao contexto do projeto:

---

### 1. Tempo de inicialização e consumo de recursos

**Quarkus vs Spring Boot:**

* Quarkus possui startup significativamente mais rápido
* Menor consumo de memória
* Otimizado para execução em containers

**Impacto no projeto:**

* Melhor desempenho em ambientes containerizados (Docker)
* Ideal para execução local leve (MVP)
* Redução de custo em ambientes cloud

---

### 2. Arquitetura cloud-native

**Quarkus vs Spring Boot:**

* Quarkus foi projetado com foco em cloud-native desde sua origem
* Melhor integração com Kubernetes e ambientes distribuídos
* Suporte a build otimizado (inclusive native image)

**Impacto no projeto:**

* Facilita evolução futura para microserviços
* Maior aderência a arquiteturas modernas

---

### 3. Produtividade no desenvolvimento

**Quarkus vs Spring Boot:**

* Quarkus possui:

    * Dev mode com hot reload
    * Configuração simplificada
* Spring Boot possui maior maturidade, porém maior complexidade em algumas configurações

**Impacto:**

* Desenvolvimento mais rápido no MVP
* Menor tempo de feedback durante desenvolvimento

---

### 4. Integração com Kotlin e DDD

**Quarkus vs Spring Boot:**

* Ambos suportam Kotlin
* Quarkus possui suporte moderno a:

    * CDI (injeção de dependência)
    * Panache (ORM simplificado)
* Menos "magia" que Spring → mais controle arquitetural

**Impacto:**

* Melhor controle sobre camadas (DDD + Hexagonal)
* Menor acoplamento a framework

---

### 5. Overhead e complexidade

**Quarkus vs Spring Boot:**

* Spring Boot possui:

    * Maior abstração
    * Maior quantidade de auto-configurações
* Quarkus é mais enxuto e explícito

**Impacto:**

* Melhor previsibilidade do comportamento
* Menor risco de efeitos colaterais ocultos

---

### 6. Aderência ao contexto do MVP

O projeto possui características:

* Escopo controlado (MVP)
* Necessidade de rápida entrega
* Execução em ambiente containerizado
* Evolução futura planejada

**Escolha:**

* Quarkus atende melhor ao equilíbrio entre:

    * Performance
    * Simplicidade
    * Evolução futura

---

## Alternatives Considered

### Spring Boot

**Prós:**

* Extremamente popular e consolidado
* Grande comunidade
* Ecossistema amplo (Spring Security, Spring Data, etc.)
* Facilidade de encontrar suporte

**Contras:**

* Maior consumo de memória
* Startup mais lento
* Maior complexidade interna
* Alto nível de abstração (menos controle explícito)
* Overkill para MVP de médio porte

---

## Consequences

### Positivas

* Alta performance e baixo consumo
* Startup rápido
* Melhor experiência em containers
* Código mais controlado e explícito
* Aderência a arquiteturas modernas (cloud-native)

---

### Negativas

* Comunidade menor comparada ao Spring
* Menor quantidade de material e exemplos
* Curva de aprendizado inicial
* Menor maturidade em alguns módulos

---

## Scope

Esta decisão se aplica a:

* Todo o backend do sistema
* Estrutura de APIs REST
* Camadas de aplicação e infraestrutura

---

## Future Considerations

* Avaliar uso de native image para otimização extrema
* Monitorar performance em produção
* Possível integração com mensageria
* Evolução para arquitetura distribuída

---

## References

* Documentação oficial do Quarkus
* Documentação oficial do Spring Boot
* Práticas de arquitetura cloud-native
