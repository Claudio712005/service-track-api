# ADR-002: Escolha do Banco de Dados - PostgreSQL

## Status

Aprovada

## Context

A oficina mecânica está desenvolvendo um Sistema Integrado de Atendimento e Execução de Serviços, com foco na gestão de:

* Ordens de Serviço (OS)
* Clientes
* Veículos
* Serviços
* Peças e controle de estoque

O sistema possui características importantes:

* Forte consistência de dados (ex: orçamento, aprovação, status da OS)
* Relacionamentos complexos (cliente → veículo → ordem de serviço → itens → peças)
* Necessidade de integridade transacional (ex: atualização de estoque ao adicionar peças)
* Consultas estruturadas (relatórios, tempo médio de execução, histórico)
* Modelo inicial monolítico
* Evolução futura possível (escala e novas integrações)

Além disso, o sistema deve evitar problemas atuais como:

* Perda de histórico
* Inconsistência de dados
* Falhas no controle de estoque
* Erros de fluxo de execução

Foram consideradas as seguintes opções de banco de dados:

* PostgreSQL (relacional)
* MySQL (relacional)
* MongoDB (NoSQL orientado a documentos)

---

## Decision

Será utilizado o PostgreSQL como banco de dados principal do sistema.

---

## Justificativa

A escolha do PostgreSQL foi baseada nos seguintes fatores técnicos e comparativos:

---

### 1. Modelagem relacional adequada ao domínio

O domínio possui forte estrutura relacional:

* Cliente possui múltiplos veículos
* Veículo possui múltiplas ordens de serviço
* Ordem de serviço possui múltiplos itens (serviços e peças)

**PostgreSQL vs MongoDB:**

* PostgreSQL permite integridade referencial nativa (FKs)
* MongoDB exigiria controle manual de relacionamentos ou duplicação de dados
* Risco maior de inconsistência no MongoDB

**PostgreSQL vs MySQL:**

* Ambos suportam modelo relacional
* PostgreSQL possui suporte mais avançado a constraints e tipos complexos

---

### 2. Suporte a transações e consistência (ACID)

Operações críticas exigem consistência:

* Aprovação de orçamento
* Atualização de estoque
* Mudança de status da OS

**PostgreSQL vs MongoDB:**

* PostgreSQL possui suporte ACID completo e maduro
* MongoDB suporta transações, porém com maior custo e complexidade
* MongoDB é mais voltado para consistência eventual

**PostgreSQL vs MySQL:**

* Ambos suportam ACID (InnoDB no MySQL)
* PostgreSQL possui melhor controle de concorrência (MVCC mais robusto)
* Maior previsibilidade em cenários com alta concorrência

---

### 3. Consultas complexas e capacidade analítica

O sistema exige:

* Relatórios operacionais
* Histórico detalhado
* Métricas (tempo médio, volume de serviços)
* Consultas com múltiplos relacionamentos

**PostgreSQL vs MongoDB:**

* PostgreSQL possui joins nativos e eficientes
* MongoDB depende de agregações mais complexas ($lookup)
* Queries relacionais são mais naturais e performáticas no PostgreSQL

**PostgreSQL vs MySQL:**

* PostgreSQL possui melhor suporte a:

    * CTEs (WITH)
    * Window functions
    * Queries analíticas avançadas
* MySQL é mais limitado nesse aspecto

---

### 4. Tipagem forte e validação de dados

O sistema lida com dados sensíveis:

* CPF/CNPJ
* Placa de veículo
* Valores monetários
* Status controlados

**PostgreSQL vs MongoDB:**

* PostgreSQL possui tipagem forte e validação no banco
* MongoDB é schema-less, aumentando risco de dados inválidos

**PostgreSQL vs MySQL:**

* PostgreSQL possui tipos mais ricos (ex: JSONB, ENUM avançado, arrays)
* Melhor suporte a validações complexas

---

### 5. Flexibilidade com estrutura híbrida

Apesar de relacional, o PostgreSQL permite:

* Uso de JSONB para dados semi-estruturados
* Indexação eficiente em campos JSON
* Evolução gradual do modelo

**Comparação:**

* PostgreSQL combina o melhor dos dois mundos (relacional + semi-estruturado)
* MySQL possui suporte a JSON, porém menos otimizado
* MongoDB é totalmente flexível, mas perde controle estrutural

---

### 6. Ecossistema e integração

* Integração nativa com Spring Boot, JPA e Hibernate
* Suporte a ferramentas de migração (Flyway, Liquibase)
* Alta compatibilidade com Docker e ambientes cloud

**Comparação:**

* PostgreSQL é amplamente adotado em sistemas enterprise modernos
* MySQL também é popular, porém com menos recursos avançados
* MongoDB exige adaptação maior no modelo DDD relacional

---

## Alternatives Considered

### MongoDB

**Prós:**

* Flexibilidade de schema
* Escalabilidade horizontal facilitada
* Boa performance para dados não relacionais

**Contras:**

* Dificuldade com relacionamentos complexos
* Ausência de joins eficientes
* Maior risco de inconsistência
* Complexidade para garantir integridade transacional
* Não aderente ao modelo fortemente relacional do domínio

---

### MySQL

**Prós:**

* Popularidade e facilidade de uso
* Boa performance em cenários simples
* Suporte ACID com InnoDB

**Contras:**

* Menor suporte a queries avançadas
* Recursos mais limitados comparado ao PostgreSQL
* Menor flexibilidade com dados semi-estruturados
* Menor robustez em cenários analíticos e complexos

---

## Consequences

### Positivas

* Integridade dos dados garantida
* Redução de inconsistências no fluxo de negócio
* Melhor suporte a regras complexas
* Alta capacidade de consulta e análise
* Forte aderência ao modelo DDD
* Base sólida para evolução do sistema

---

### Negativas

* Necessidade de maior cuidado na modelagem relacional
* Escalabilidade horizontal mais complexa que NoSQL
* Possível necessidade de tuning (índices, queries)

---

## Scope

Esta decisão se aplica a:

* Persistência principal do sistema (MVP)
* Todos os módulos: clientes, veículos, ordens de serviço, estoque

---

## Future Considerations

* Uso de cache (ex: Redis)
* Implementação de CQRS para leitura otimizada
* Monitoramento de performance (queries lentas)
* Avaliação de NoSQL para logs/eventos
* Possível uso de mensageria para desacoplamento

---

## References

* Documentação oficial do PostgreSQL
* Boas práticas de modelagem relacional
* Princípios de Domain-Driven Design (DDD)