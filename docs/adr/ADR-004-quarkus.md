# ADR – 004: Framework Backend — Quarkus

## Data
18/04/2026

---

## Status

- Aceita

---

## Contexto

O sistema é um monolito modular containerizado, baseado em DDD e Arquitetura Hexagonal. O framework precisa oferecer alta performance, baixo consumo de recursos, suporte a Kotlin, dev experience produtiva e integração com o ecossistema JVM moderno, mantendo controle explícito sobre as camadas arquiteturais.

---

## Decisão

Adotar **Quarkus** como framework principal do back-end.

Fatores determinantes:
- Startup rápido e baixo consumo de memória, otimizado para containers Docker
- Projetado para cloud-native desde sua concepção (Kubernetes, GraalVM native image)
- Dev mode com hot reload para ciclos de feedback curtos no desenvolvimento
- CDI e Panache ORM com suporte explícito a Kotlin
- Menor abstração implícita que Spring Boot, favorecendo controle arquitetural (DDD + Hexagonal)

---

## Consequências

### Positivas
- Alta performance e baixo footprint em ambientes containerizados
- Startup rápido reduz tempo de deploy e ciclos de desenvolvimento
- Código mais explícito e controlado, com menor acoplamento ao framework
- Aderência nativa a padrões cloud-native e preparação para escala

### Negativas
- Ecossistema e comunidade menores que Spring Boot
- Menor volume de material de referência, exemplos e Stack Overflow
- Curva de aprendizado inicial, especialmente em configurações avançadas
- Alguns módulos com menor maturidade comparados aos equivalentes Spring

---

## Alternativas Consideradas

### Opção 1: Spring Boot
- Framework mais popular e consolidado do ecossistema Java/Kotlin
- Prós: comunidade enorme, ecossistema completo (Security, Data, Cloud), ampla documentação e suporte
- Contras: maior consumo de memória, startup mais lento, alto nível de abstração implícita; overhead excessivo para MVP de médio porte com equipe reduzida

---

## Referências

- [RFC-004 – Adoção do Quarkus como Framework Backend](../rfc/RFC-004-quarkus.md)
- Documentação oficial do Quarkus — quarkus.io
- Documentação oficial do Spring Boot — spring.io
- Práticas de arquitetura cloud-native
