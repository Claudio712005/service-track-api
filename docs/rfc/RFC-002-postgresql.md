# RFC – 002: PostgreSQL como Banco de Dados Principal

## Data
18/04/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para adoção do PostgreSQL como banco de dados relacional principal do sistema de gestão de ordens de serviço, com objetivo de garantir integridade transacional, suporte a relacionamentos complexos e capacidade analítica nativa em um modelo DDD fortemente relacional.

---

## Problema

O domínio do sistema possui características que impõem requisitos rigorosos ao banco de dados:

- Relacionamentos complexos e profundos: Cliente → Veículo → OS → Itens → Peças/Serviços
- Operações transacionalmente críticas: aprovação de orçamento altera status da OS e reserva estoque atomicamente
- Necessidade de consistência imediata (não eventual) nos fluxos de negócio
- Requisitos analíticos: tempo médio de execução por tipo de serviço, volume por período, histórico completo de OS
- Dados sensíveis com validação forte: CPF, valores monetários, status controlados por máquina de estados

Soluções inadequadas para este contexto provocariam:
- Inconsistências no controle de estoque (peças reservadas vs. disponíveis)
- Perda de histórico transacional de ordens de serviço
- Dificuldade em queries relacionais complexas para relatórios operacionais

---

## Proposta Técnica

### Banco de dados escolhido: PostgreSQL

#### Integridade relacional e transacional

- Foreign Keys nativas garantem integridade referencial em todos os níveis da hierarquia
- Suporte ACID completo com MVCC (Multi-Version Concurrency Control) robusto
- Transações explícitas para operações críticas: uma única transação cobre aprovação de orçamento + atualização de status + reserva de estoque
- Isolamento de transações configurável (Read Committed como padrão, Serializable quando necessário)

#### Capacidade analítica

- CTEs (Common Table Expressions) com `WITH` para queries hierárquicas complexas
- Window Functions (`ROW_NUMBER`, `RANK`, `LAG`, `LEAD`) para análises temporais e rankings
- Aggregations eficientes para relatórios operacionais sem camada adicional de processamento

#### Tipos avançados

- `JSONB` com indexação GIN para dados semi-estruturados (ex.: metadados de OS)
- `UUID` nativo para identificadores de entidades de domínio
- `ENUM` e `CHECK constraints` para validação de estados controlados no banco
- Tipos monetários precisos (`NUMERIC`) sem risco de arredondamento de ponto flutuante

#### Integração com o stack técnico

- Quarkus Panache com Hibernate ORM: integração nativa e testada
- Flyway: migrations versionadas com rollback controlado
- Testcontainers: instância PostgreSQL real em testes de integração
- pgAdmin / DBeaver: ferramentas de administração e inspeção amplamente disponíveis

#### Modelagem prevista

```
usuario          → tabela base com discriminator (cliente / mecanico)
veiculo          → FK para usuario (cliente)
ordem_servico    → FK para veiculo, mecanico responsável
item_os          → FK para ordem_servico (serviços e peças aplicados)
catalogo_servico → entidade independente (referência em item_os)
insumo           → controle de estoque com quantidade e estoque mínimo
```

### Estratégia de migração com Flyway

- Cada alteração de schema é versionada em arquivo SQL numerado (`V1__init.sql`, `V2__add_index.sql`)
- Migrations executadas automaticamente no startup da aplicação
- Rollback via migration reversa quando necessário
- Ambientes de dev, test e produção usam o mesmo pipeline de migration

---

## Alternativas Consideradas

### Opção 1: MongoDB

- Banco NoSQL orientado a documentos com schema dinâmico
- Prós: flexibilidade de schema para dados heterogêneos, escalabilidade horizontal simplificada com sharding, boa performance em leitura de documentos auto-contidos
- Contras: relacionamentos profundos exigem `$lookup` (joins custosos) ou desnormalização com duplicação de dados; transações multi-documento com overhead significativo; schema-less aumenta risco de dados inválidos sem validação estrutural; não aderente ao modelo DDD relacional do domínio

---

### Opção 2: MySQL

- Banco relacional popular com suporte ACID via InnoDB
- Prós: enorme popularidade, facilidade de uso, suporte ACID maduro, ampla documentação
- Contras: MVCC menos sofisticado que PostgreSQL em cenários de alta concorrência; suporte inferior a CTEs e Window Functions (melhorou no MySQL 8 mas ainda limitado); JSONB inexistente (apenas JSON sem indexação GIN eficiente); CHECK constraints ignoradas em versões antigas; menor riqueza de tipos nativos

---

## Pontos em Aberto

- Estratégia de backup e point-in-time recovery (PITR) para o ambiente de produção
- Política de retenção e arquivamento de ordens de serviço antigas
- Avaliação de uso de cache (Redis) para leituras de alta frequência (ex.: catálogo de serviços)
- Monitoramento de queries lentas: configuração de `pg_stat_statements` e alertas
- Avaliação futura de CQRS para separar modelo de leitura (relatórios) do modelo de escrita

---

## Impactos

### Positivos
- Integridade referencial e transacional garantidas por design do banco
- Modelo de dados fortemente aderente ao domínio DDD
- Consultas analíticas complexas sem necessidade de camada adicional de processamento
- Ecossistema maduro com ferramentas consolidadas de migração, monitoramento e administração

### Negativos
- Escalabilidade horizontal mais complexa: requer replicação, connection pooling (PgBouncer) e eventual sharding
- Schema evolution exige disciplina e planejamento cuidadoso de migrations
- Performance sob carga intensa requer tuning de índices, `VACUUM` e configurações do servidor

---

## Próximos Passos

- Revisão pelo time de dados e backend
- Validação do modelo de dados inicial (entidades e relacionamentos)
- Configuração do Flyway no pipeline de CI/CD
- Definição de estratégia de backup e monitoramento para produção
- Aprovação formal e geração da ADR-002 correspondente
