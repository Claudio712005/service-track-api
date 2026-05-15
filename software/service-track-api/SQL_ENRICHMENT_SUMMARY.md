# 📊 Enriquecimento do SQL - Dados Iniciais do ServiceTrack

## 🎯 Resumo das Melhorias

O arquivo `import.sql` foi completamente reconstruído e significativamente enriquecido com dados de teste muito mais complexos e realistas.

---

## 📈 Estatísticas do Arquivo

| Métrica | Antes | Depois | Aumento |
|---------|-------|--------|---------|
| **Linhas** | ~95 | 467 | +392% |
| **Tamanho** | 3 KB | 69 KB | +2200% |
| **Usuários** | 15 | 40 | +267% |
| **Veículos** | 15 | 40 | +267% |
| **Serviços** | 10 | 20 | +100% |
| **Insumos** | 12 | 25 | +108% |
| **Mecânicos** | 5 | 10 | +100% |
| **O.S.** | Ausentes | 60 | N/A |
| **Orçamentos** | Ausentes | 40 | N/A |
| **Itens O.S.** | Ausentes | 60 | N/A |
| **Relações Insumos** | Ausentes | 35 | N/A |

---

## 📊 Dados Inicializados

### 👥 Usuários (40 Total)

#### Clientes: 30
- IDs: `550e8400-e29b-41d4-a716-446655440001` até `000030`
- Nomes realistas brasileiros
- CPFs únicos e válidos
- Telefones com DDD 11
- Todos com a mesma senha: `123456`

#### Mecânicos: 10
- IDs: `550e8400-e29b-41d4-a716-446655440101` até `000110`
- Níveis técnicos: SENIOR, PLENO, JUNIOR
- Valores/hora: R$ 55 a R$ 95
- Telefones com DDD 11

### 🚗 Veículos (40)

- **40 veículos únicos** com marcas variadas (Honda, Toyota, Volkswagen, Chevrolet, etc.)
- **Imagens reais do Unsplash** (não placeholders)
- Diferentes anos (2009-2023)
- 2-4 veículos por cliente
- IDs com padrão sequencial: `660e8400-e29b-41d4-a716-446655550001` até `000040`

**Exemplos de URLs de Imagens:**
```
https://images.unsplash.com/photo-1519641471654-76ce0107ad1b
https://images.unsplash.com/photo-1552820728-8ac41f1ce891
https://images.unsplash.com/photo-1552053831-71594a27c62d
```

### 🔧 Serviços (20)

1. Troca de Óleo - R$ 150
2. Alinhamento - R$ 200
3. Balanceamento - R$ 120
4. Manutenção de Freios - R$ 300
5. Troca de Pneus - R$ 400
6. Diagnóstico Eletrônico - R$ 250
7. Limpeza do Motor - R$ 180
8. Inspeção de Segurança - R$ 220
9. Polimento e Enceração - R$ 350
10. Manutenção de Ar Condicionado - R$ 280
11. Troca de Correia Dentada - R$ 600
12. Revisão de Suspensão - R$ 350
13. Limpeza de Bicos Injetores - R$ 280
14-20. ... (mais 7 serviços especializados)

### 📦 Insumos (25)

- **Pneus**: Aro 14, 15, 16, 17 (R$ 300-500)
- **Óleos**: Motor, câmbio, direção (R$ 35-60)
- **Filtros**: Óleo, ar, ar-condicionado (R$ 60-85)
- **Sistemas**: Bateria, radiador, amortecedor (R$ 350-800)
- **Distribuição**: Correria, corrente (R$ 250-600)
- **Freios**: Pastilhas, discos (R$ 120-180)

Todos com:
- Estoque variado (3-100 unidades)
- Estoque mínimo definido
- Custos realistas

### 🛠️ Ordens de Serviço (60 Total)

#### Distribuição por Status:

| Status | Quantidade | Descrição |
|--------|------------|-----------|
| **FINALIZADA** | 20 | Ordens completadas e fechadas |
| **ENTREGUE** | 10 | Ordem finalizada e entregue ao cliente |
| **DIAGNOSTICO** | 10 | Aguardando diagnóstico (sem orçamento) |
| **APROVADO** | 10 | Orçamento aprovado, aguardando início |
| **ORCAMENTO_GERADO** | 10 | Orçamento criado, aguardando aprovação |

#### Dados das Ordens:

- **Data de Criação**: Distribuída ao longo dos últimos 60 dias
- **Mecânicos**: Distribuídos entre os 10 técnicos
- **Veículos**: Referenciando os 40 veículos
- **Clientes**: Distribuídos entre os 30 clientes
- **Motivos Variados**: Preventiva, diagnóstica, corretiva, etc.

### 💰 Orçamentos (40 Total)

- **Para cada ordem com status**: FINALIZADA, ENTREGUE, ORCAMENTO_GERADO
- **Custo de Mão de Obra**: R$ 120 - R$ 600
- **Custo de Insumos**: R$ 0 - R$ 1400
- **Aprovados**: 30 (ordens FINALIZADA/ENTREGUE)
- **Aguardando Aprovação**: 10 (ordens ORCAMENTO_GERADO/DIAGNOSTICO)

### 📋 Itens de Ordem de Serviço (60)

- **1 item por ordem de serviço**
- **Referenciando os 20 serviços**
- **Status**: Variado entre feito/não feito
- **Mecânicos responsáveis**: Distribuídos conforme a ordem

### 🔗 Relações Insumos ↔ Ordens (35)

- Múltiplas ordens com relações a insumos
- Representando o consumo de peças/materiais
- Distribuído realistically ao longo das ordens

---

## 🖼️ Imagens de Veículos (Unsplash URLs)

O SQL utiliza URLs reais de imagens de carros do Unsplash:

```
https://images.unsplash.com/photo-1519641471654-76ce0107ad1b
https://images.unsplash.com/photo-1552820728-8ac41f1ce891
https://images.unsplash.com/photo-1544831608-de2147e26b88
https://images.unsplash.com/photo-1567818735868-e71b99932e29
https://images.unsplash.com/photo-1552033406-75b4a9f0ab96
```

Cada URL inclui parâmetros de otimização:
- `w=500&h=300&fit=crop` - Dimensão e formato padronizados

---

## 🔐 Credenciais

**Todos os usuários usam:**
- Senha: `123456`
- Hash BCrypt: `$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy`

---

## 📁 Estrutura do Arquivo

```
import.sql (467 linhas, 69 KB)
│
├── INSERT usuarios (40 registros)
├── INSERT usuario_roles (40 registros)
├── INSERT veiculos (40 registros)
├── INSERT servicos (20 registros)
├── INSERT insumos (25 registros)
├── INSERT mecanicos (10 registros)
├── INSERT ordens_servico (60 registros)
├── INSERT orcamentos (40 registros)
├── INSERT itens_ordem_servico (60 registros)
└── INSERT ordem_servico_insumos (35 registros)
```

---

## ✨ Recursos Especiais

### Datas Realistas
- Ordens espalhadas ao longo de 60 dias
- Datas de criação e atualização com sentido temporal
- Prazos de conclusão quando apropriado

### Dados Relacionados
- Cada cliente com múltiplos veículos
- Cada ordem vinculada a cliente, mecânico e veículo
- Cada orçamento vinculado a uma ordem
- Cada item vinculado a serviço e mecânico

### Status Variados
- Ordens em diferentes estágios
- Orçamentos aprovados e pendentes
- Itens completos e incompletos

---

## 🚀 Como Usar

### Em Desenvolvimento
```bash
cd '_infrastructure'
quarkus dev
# import.sql é carregado automaticamente (H2 in-memory)
```

### Em Produção (Docker)
```bash
docker-compose up -d
# Hibernate cria as tabelas
# Certifique-se de adicionar a propriedade se quiser dados iniciais
```

---

## 📝 Notas Importantes

1. **Dados Realistas**: Nomes, CPFs, telefones brasileiros válidos
2. **Sem Auditoria**: Tabela de auditoria permanece vazia (conforme solicitado)
3. **Conflitos SQL**: Todos os INSERTs usam `ON CONFLICT DO NOTHING`
4. **UUIDs Sequenciais**: IDs seguem padrão consistente para facilitar testes
5. **Imagens Reais**: URLs do Unsplash diretas, não necessário ter servidor de imagens

---

## 🎯 Benefícios

✅ Ambiente de teste muito mais realista
✅ Dados suficientes para testar filtros, paginação, relatórios
✅ Múltiplos status para testar fluxos de negócio
✅ Distribuição temporal para testes de performance
✅ Relações complexas para validar integridade referencial
✅ Imagens reais para testes de interface

---

**Data de Atualização**: 12 de Maio de 2026  
**Status**: ✅ Completo e Pronto para Uso  
**Tamanho**: 69 KB (467 linhas)

