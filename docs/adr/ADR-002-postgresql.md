# ADR – 002: Banco de Dados Principal — PostgreSQL

## Data
18/04/2026

---

## Status

- Aceita

---

## Contexto

O domínio é fortemente relacional (Cliente → Veículo → OS → Itens → Estoque) e exige integridade transacional em operações críticas como aprovação de orçamento e controle de estoque. O sistema requer suporte a consultas analíticas, histórico detalhado e métricas operacionais. Inicia como monolito modular com possibilidade de evolução futura.

---

## Decisão

Adotar **PostgreSQL** como banco de dados principal do sistema.

Fatores determinantes:
- Suporte ACID completo e maduro com MVCC robusto
- Integridade referencial nativa via Foreign Keys, aderente ao modelo DDD
- Suporte a CTEs, Window Functions e JSONB para consultas analíticas avançadas
- Integração consolidada com JPA/Hibernate, Flyway e ecossistema Quarkus/Panache

---

## Consequências

### Positivas
- Integridade e consistência dos dados garantidas por design
- Modelo relacional plenamente aderente ao domínio
- Alta capacidade analítica com queries complexas nativamente eficientes
- Ecossistema maduro com ferramentas de migração e monitoramento

### Negativas
- Escalabilidade horizontal mais complexa que soluções NoSQL
- Modelagem relacional exige maior rigor no design do schema
- Possível necessidade de tuning de índices e queries em cenários de alta carga

---

## Alternativas Consideradas

### Opção 1: MongoDB
- Banco NoSQL orientado a documentos com schema flexível
- Prós: flexibilidade de schema, escalabilidade horizontal facilitada
- Contras: joins ineficientes via `$lookup`, consistência transacional limitada, incompatível com o modelo fortemente relacional do domínio

### Opção 2: MySQL
- Banco relacional amplamente popular
- Prós: popularidade, suporte ACID com InnoDB, facilidade de uso
- Contras: MVCC menos robusto, suporte inferior a queries analíticas avançadas, JSONB menos otimizado

---

## Referências

- [RFC-002 – PostgreSQL como Banco de Dados Principal](../rfc/RFC-002-postgresql.md)
- Documentação oficial do PostgreSQL — postgresql.org
- Domain-Driven Design — Eric Evans
- Flyway — ferramenta de migração de banco de dados
