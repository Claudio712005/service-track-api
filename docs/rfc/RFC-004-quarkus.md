# RFC – 004: Adoção do Quarkus como Framework Backend

## Data
18/04/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para adoção do Quarkus como framework principal do back-end do sistema de gestão de ordens de serviço, com objetivo de obter alta performance em containers, máximo controle arquitetural sobre as camadas DDD/Hexagonal e produtividade no desenvolvimento do MVP.

---

## Problema

A escolha do framework impacta diretamente:

- Performance operacional: tempo de startup, consumo de memória em containers Docker
- Custo de infraestrutura: containers mais leves reduzem custo de cloud
- Controle arquitetural: frameworks com alto nível de abstração implícita dificultam enforcement de regras de dependência (DDD + Hexagonal)
- Produtividade no desenvolvimento: ciclo de feedback curto é crítico no MVP
- Evolução futura: o framework deve suportar tanto o modelo monolítico atual quanto uma eventual decomposição

Frameworks com excessive "magic" (auto-configuração opaca) tendem a criar acoplamento oculto ao framework, violando princípios da Arquitetura Hexagonal onde a infraestrutura deve ser facilmente substituível.

---

## Proposta Técnica

### Framework escolhido: Quarkus

#### Performance e eficiência em containers

Quarkus foi projetado para o modelo cloud-native, com otimizações em tempo de build:

| Métrica            | Quarkus (JVM) | Spring Boot (JVM) |
|--------------------|---------------|-------------------|
| Tempo de startup   | ~0,8s         | ~3–5s             |
| Memória RSS        | ~150 MB       | ~300+ MB          |
| Native image*      | ~0,05s / 50MB | Suporte limitado  |

*GraalVM native image: opção futura para ambientes com requisitos de startup extremamente baixos

#### Integração com o stack técnico do projeto

```
Kotlin           → suporte oficial, integração testada com K2 compiler
Panache ORM      → active record pattern simplificado sobre Hibernate
RESTEasy         → implementação JAX-RS com suporte a geração de código OpenAPI
SmallRye JWT     → integração nativa com JWT + chaves assimétricas
CDI (Weld)       → injeção de dependência explícita e controlada
Flyway           → migration de banco de dados integrada ao startup
Testcontainers   → suporte nativo para testes de integração com PostgreSQL real
```

#### Controle explícito favorece DDD + Hexagonal

O Quarkus não injeta beans automaticamente por classpath scanning irrestrito; a configuração é explícita via anotações `@ApplicationScoped`, `@RequestScoped`, `@Produces`. Isso:

- Torna as dependências entre componentes visíveis e auditáveis
- Facilita enforcement da regra de dependência: `_domain` → `_application` → `_infrastructure`
- Elimina beans registrados "magicamente" que violam boundaries arquiteturais

#### Dev mode e produtividade

```bash
# Hot reload automático — sem restart manual durante desenvolvimento
./gradlew quarkusDev
```

- Alterações em código Kotlin são refletidas sem reiniciar a JVM
- Quarkus Dev UI disponível em `localhost:8080/q/dev` para inspeção de beans, configurações e extensões ativas
- Dev Services: sobe instância Docker de PostgreSQL automaticamente em dev mode sem configuração manual

#### OpenAPI e geração de código

- Especificação OpenAPI definida em YAML versionada no repositório
- Plugin `openapi-generator` gera interfaces de controller e DTOs em tempo de build
- Controllers implementam as interfaces geradas, garantindo conformidade com o contrato da API
- Documentação Swagger UI disponível automaticamente em dev/test

---

## Alternativas Consideradas

### Opção 1: Spring Boot

- Framework mais popular e consolidado do ecossistema Java/Kotlin
- Prós: comunidade enorme, ecossistema completo (Spring Security, Spring Data, Spring Cloud), vasta documentação, facilidade de contratação, maturidade comprovada em sistemas enterprise de grande porte
- Contras: maior consumo de memória (2–3x Quarkus em JVM mode); startup mais lento dificulta scaling horizontal rápido; auto-configuração implícita (Spring Boot auto-config) pode criar acoplamento oculto ao framework; complexidade interna maior dificulta debugging de comportamentos inesperados; overhead excessivo para MVP com equipe reduzida e escopo controlado

---

## Pontos em Aberto

- Avaliação do uso de GraalVM native image em produção: trade-off entre tempo de compilação elevado e performance extrema em runtime
- Estratégia de integração com mensageria (SmallRye Reactive Messaging / Kafka) para evolução futura
- Política de uso de Quarkus Dev Services em ambientes de CI: substituir por Testcontainers explícitos para maior controle
- Monitoramento em produção: configuração de Micrometer + Prometheus + Grafana com extensão `quarkus-micrometer`
- Avaliação de Quarkus REST Client Reactive para integração com serviços externos quando necessário

---

## Impactos

### Positivos
- Menor custo de infraestrutura por container com footprint reduzido
- Ciclo de desenvolvimento mais rápido com Dev mode e hot reload
- Controle explícito sobre dependências favorece a integridade arquitetural do DDD + Hexagonal
- Preparação para cloud-native e eventual decomposição em microsserviços

### Negativos
- Comunidade e volume de conteúdo (Stack Overflow, blogs, tutoriais) menores que Spring Boot
- Curva de aprendizado para equipes com background exclusivo em Spring
- Alguns módulos Quarkus têm menor maturidade: comportamentos inesperados em cenários edge-case
- Suporte a algumas bibliotecas Java não certificadas pode exigir workarounds

---

## Próximos Passos

- Revisão pelo time de backend e infraestrutura
- Avaliação do impacto de Quarkus Dev Services no pipeline de CI
- Definição da estratégia de monitoramento e observabilidade em produção
- Aprovação formal e geração da ADR-004 correspondente
