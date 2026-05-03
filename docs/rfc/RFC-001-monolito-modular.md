# RFC – 001: Monolito Modular como Arquitetura Inicial

## Data
18/04/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para adoção de arquitetura Monolito Modular estruturado com DDD e Arquitetura Hexagonal como base do sistema de gestão de ordens de serviço, com objetivo de maximizar produtividade no MVP enquanto preserva capacidade de evolução gradual para microsserviços.

---

## Problema

O sistema parte do zero como MVP com equipe reduzida e domínio de negócio ainda em fase de validação. As principais dificuldades a resolver são:

- Boundaries do domínio incertos no estágio inicial, tornando prematura qualquer decomposição em serviços
- Forte acoplamento transacional entre entidades (OS ↔ Estoque ↔ Itens) que, em microsserviços, exigiria Saga Pattern ou Two-Phase Commit
- Ausência de infraestrutura de observabilidade distribuída (tracing, service mesh, circuit breaker)
- Custo de operação e deploy de múltiplos serviços inviável com equipe e orçamento do estágio inicial
- Risco de overengineering prematuro comprometendo velocidade de entrega do MVP

---

## Proposta Técnica

Adotar Monolito Modular com separação explícita de módulos Gradle independentes:

### Estrutura de módulos

```
_domain         → Entidades, Value Objects, Aggregates, Domain Services
                  Sem dependências externas; núcleo puro do negócio

_application    → Use Cases, Ports (in/out), Application Services
                  Orquestra o domínio; desconhece infraestrutura

_infrastructure → REST Adapters, Persistência (Panache/JPA), Segurança, Config
                  Implementa os ports; única camada com dependências externas
```

### Princípios aplicados

- **DDD**: bounded contexts por domínio funcional (usuario, veiculo, os, estoque, catalogo)
- **Arquitetura Hexagonal**: inversão de dependência via ports/adapters — infraestrutura depende da aplicação, nunca o contrário
- **Regra de dependência**: `_domain` não conhece `_application`; `_application` não conhece `_infrastructure`
- **Separação em módulos Gradle**: cada camada é um artefato isolado com seu próprio `build.gradle.kts`, evitando acoplamento acidental

### Comunicação entre módulos internos

- Comunicação direta via chamada de método (sem mensageria nesta fase)
- Application Services invocam Domain Services e Repositories via interfaces (ports)
- Não há comunicação entre bounded contexts por eventos nesta fase; quando necessário, será introduzido um barramento in-memory antes da extração para serviços

### Estratégia de evolução futura — Strangler Pattern

Quando um módulo apresentar necessidade clara de escala independente:

1. Identificar o bounded context candidato à extração (maior carga ou time dedicado)
2. Definir contrato de API ou contrato de eventos para o módulo futuro
3. Introduzir comunicação assíncrona interna (barramento in-memory → Kafka/RabbitMQ)
4. Extrair o módulo para serviço autônomo com banco de dados próprio
5. Manter compatibilidade retroativa durante a transição (strangler facade)
6. Remover o código legado após validação completa

### Critérios objetivos que disparam avaliação de extração

- Carga sustentada e crescente isolada em um único bounded context
- Necessidade de times independentes atuando em domínios diferentes
- Gargalos de deploy afetando entrega de funcionalidades não relacionadas
- Necessidade de escala com tecnologia diferente (ex.: módulo de processamento com requisitos de latência extrema)

---

## Alternativas Consideradas

### Opção 1: Microsserviços desde o início

- Decomposição imediata em serviços autônomos por bounded context
- Prós: escala independente por domínio, deploy totalmente desacoplado, isolamento de falhas em produção, preparação antecipada para crescimento
- Contras: boundaries ainda indefinidos aumentam risco de cortes errados (distributed monolith); consistência eventual exige Saga Pattern; necessidade de API Gateway, service discovery, distributed tracing; custo operacional incompatível com o estágio inicial

---

### Opção 2: Monolito tradicional (sem modularização interna)

- Sistema único sem separação explícita de módulos ou camadas reforçadas por artefatos
- Prós: máxima simplicidade técnica inicial, sem overhead de configuração de módulos
- Contras: acoplamento crescente torna evolução progressivamente mais custosa; ausência de boundaries explícitos dificulta identificação de bounded contexts para extração futura; dívida técnica acumula rapidamente

---

## Pontos em Aberto

- Definição formal dos bounded contexts à medida que o domínio evolui
- Estratégia de versionamento de API REST para suportar extração de serviços sem breaking changes
- Política de comunicação entre bounded contexts internos: chamada direta vs. eventos in-memory
- Critério formal e processo de decisão para iniciar extração de um módulo para microsserviço
- Adoção de eventos de domínio internos para desacoplar módulos antes da separação física

---

## Impactos

### Positivos
- Entrega do MVP em menor tempo com equipe reduzida e menor overhead operacional
- Consistência transacional (ACID) garantida sem padrões complexos de compensação
- Menor custo de infraestrutura no estágio inicial
- Boundaries internos preservam capacidade de extração futura sem reescrita

### Negativos
- Escalabilidade horizontal restrita ao deploy do monolito completo
- Disciplina arquitetural contínua necessária para evitar degradação dos boundaries internos
- Risco de acúmulo de dívida técnica se a modularização não for mantida rigorosamente

---

## Próximos Passos

- Revisão pelo time de arquitetura
- Coleta de feedback sobre a estratégia de evolução e critérios de extração
- Definição dos bounded contexts iniciais e documentação nos módulos
- Aprovação formal e geração da ADR-001 correspondente
- (Se aprovado) configuração dos módulos Gradle e enforcement das regras de dependência
