# Software Requirements Specification (SRS)

## Sistema de Gestão de Ordens de Serviço e Insumos

**Empresa:** Auto Center ABC
**Autor:** Cláudio da Silva Araújo Filho
**Versão:** 1.0
**Status:** Draft
**Data:** 08/04/2026

---

## Sumário

* [1. Introdução](#1-introdução)
* [2. Visão Geral do Produto](#2-visão-geral-do-produto)
* [3. Requisitos do Sistema](#3-requisitos-do-sistema)
* [4. Premissas](#4-premissas)
* [5. Restrições](#5-restrições)
* [6. Dependências](#6-dependências)
* [7. Requisitos Não Funcionais](#7-requisitos-não-funcionais)

---

## 1. Introdução

### 1.1 Propósito

Este documento descreve os requisitos funcionais e não funcionais do sistema de gestão de ordens de serviço e insumos do Auto Center ABC, com foco no desenvolvimento do MVP do back-end.

---

### 1.2 Escopo

O sistema permitirá:

* Gestão de clientes e veículos
* Abertura e acompanhamento de ordens de serviço
* Controle de peças e insumos
* Gestão de orçamentos e autorizações
* Rastreamento de status em tempo real
* Geração de eventos de mudança de status

---

### 1.3 Definições

* **Ordem de Serviço (OS):** Registro formal de serviços realizados
* **Insumos:** Peças e materiais utilizados
* **MVP:** Produto mínimo viável

---

### 1.4 Referências

* IEEE 830
* Domain-Driven Design (DDD)

---

## 2. Visão Geral do Produto

### 2.1 Perspectiva do Produto

O sistema substituirá processos manuais e planilhas, tornando-se o núcleo operacional da oficina.

Será composto por:

* API REST (back-end)
* Integração futura com front-end

---

### 2.2 Funções do Produto

* Cadastro de clientes
* Cadastro de veículos
* Abertura de ordens de serviço
* Controle de status
* Gestão de estoque
* Geração de orçamentos
* Autorização de serviços

---

### 2.3 Usuários do Sistema

* Mecânicos
* Atendentes
* Gestores
* Clientes (futuro)

---

### 2.4 Restrições Gerais

* MVP focado em back-end
* Comunicação via API REST

---

## 3. Requisitos do Sistema

### 3.1 Requisitos Funcionais

#### RF01 – Cadastro de Cliente

O sistema deve permitir cadastrar, editar, consultar e desativar clientes.

---

#### RF02 – Cadastro de Veículo

O sistema deve permitir associar veículos a clientes.

---

#### RF03 – Abertura de Ordem de Serviço

O sistema deve permitir:

* Criar OS com cliente e veículo
* Gerar identificador único
* Registrar data/hora
* Iniciar com status "Aberta"

---

#### RF04 – Atualização de Status

* Permitir alteração conforme fluxo definido
* Impedir transições inválidas
* Registrar histórico
* Registrar responsável

**Fluxo:**

* Aberta → Em diagnóstico
* Em diagnóstico → Aguardando aprovação
* Aguardando aprovação → Em execução
* Em execução → Finalizada
* Qualquer status → Cancelada

---

#### RF05 – Gestão de Insumos

* Registrar entrada de peças
* Registrar saída de peças
* Impedir saída sem estoque
* Manter histórico

---

#### RF06 – Geração de Orçamento

* Gerar orçamento automaticamente
* Calcular valor total
* Vincular à OS

---

#### RF07 – Autorização de Serviço

* Registrar aprovação/reprovação
* Registrar data/hora
* Impedir execução sem aprovação

---

#### RF08 – Histórico

* Histórico por cliente
* Histórico por veículo
* Histórico de consumo de peças

---

#### RF09 – Notificação de Eventos

* Gerar eventos a cada mudança de status
* Permitir integração futura

---

## 4. Premissas

* Usuários possuem conhecimento básico
* Infraestrutura mínima disponível
* Dados serão validados na entrada
* Integrações externas fora do MVP

---

## 5. Restrições

* Uso de Kotlin + Quarkus
* Arquitetura baseada em DDD
* Monolito modular
* Arquitetura hexagonal
* Clean Architecture
* Contract-first
* Banco PostgreSQL
* API REST
* Autenticação JWT
* Sem interface gráfica no MVP

---

## 6. Dependências

* Banco de dados PostgreSQL
* Infraestrutura de hospedagem
* Sistema front-end futuro
* Possível mensageria

---

## 7. Requisitos Não Funcionais

### 7.1 Performance

* Tempo de resposta ≤ 2s
* Até 50 usuários simultâneos

---

### 7.2 Segurança

* Autenticação JWT
* Expiração de token
* RBAC
* Proteção contra injeção
* Logs de autenticação

---

### 7.3 Disponibilidade

* 99% de uptime mensal

---

### 7.4 Escalabilidade

* Preparado para escala horizontal

---

### 7.5 Manutenibilidade

* Código modular (DDD)
* Cobertura de testes:

    * Domain: 90%
    * Application: 80%
    * Infrastructure: 80%

---

### 7.6 Auditoria

Logs obrigatórios:

* Alterações de status
* Movimentação de estoque
* Aprovações
* Ações de usuários
