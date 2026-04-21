# ADR – 003: Linguagem de Programação — Kotlin

## Data
18/04/2026

---

## Status

- Aceita

---

## Contexto

O back-end segue arquitetura DDD com ênfase em modelagem de domínio rica, alta manutenibilidade e redução de erros em produção. A linguagem precisa oferecer alta produtividade, segurança de tipos, suporte natural a modelagem imutável e integração com o ecossistema Java (Quarkus, bibliotecas, ferramentas).

---

## Decisão

Adotar **Kotlin** como linguagem principal de desenvolvimento do back-end.

Fatores determinantes:
- Null safety nativa elimina `NullPointerException` em tempo de compilação
- `data class` e `sealed class` facilitam Value Objects e modelagem de domínio imutável
- Redução drástica de boilerplate em relação ao Java (sem Lombok)
- 100% interoperável com o ecossistema JVM (Quarkus, Panache, bibliotecas Java)

---

## Consequências

### Positivas
- Código mais conciso, expressivo e seguro por design
- Modelagem DDD mais natural: Value Objects, sealed classes, imutabilidade
- Menor propensão a erros críticos relacionados a null
- Maior produtividade da equipe no MVP

### Negativas
- Curva de aprendizado para equipes com background exclusivamente Java
- Base de desenvolvedores experientes menor que Java no mercado
- Algumas bibliotecas Java podem requerer adaptações menores de interoperabilidade

---

## Alternativas Consideradas

### Opção 1: Java
- Linguagem consolidada e dominante no ecossistema JVM
- Prós: grande comunidade, documentação ampla, estabilidade e maturidade
- Contras: alto boilerplate, propensão a erros com null em runtime, menor produtividade; depende de Lombok para reduzir verbosidade, adicionando complexidade de build

---

## Referências

- [RFC-003 – Adoção do Kotlin como Linguagem Principal](../rfc/RFC-003-kotlin.md)
- Documentação oficial do Kotlin — kotlinlang.org
- Domain-Driven Design — Eric Evans
