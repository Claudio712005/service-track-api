# ADR-005: Estratégia de Autenticação - JWT com Chaves Assimétricas

## Status

Aprovada

---

## Context

O sistema de gestão de ordens de serviço será desenvolvido como um MVP, com foco em:

* Entrega rápida de valor
* Simplicidade operacional
* Baixo custo de infraestrutura
* Facilidade de desenvolvimento e deploy

O sistema possui requisitos de segurança como:

* Autenticação de usuários administrativos
* Proteção de endpoints da API
* Controle de acesso baseado em perfil (RBAC)
* Garantia de integridade dos tokens

Neste momento, o sistema:

* Não possui necessidade de Single Sign-On (SSO)
* Não exige integração com múltiplos provedores de identidade
* Possui escopo controlado de usuários
* Será consumido inicialmente por clientes internos ou aplicações simples

Foram consideradas as seguintes abordagens:

* JWT com chaves assimétricas (public/private key)
* Uso de Identity Provider externo (Keycloak)

---

## Decision

Será adotado o uso de **JWT (JSON Web Token) com assinatura utilizando chaves assimétricas (private key / public key)** como mecanismo de autenticação inicial do sistema.

---

## Justificativa

### 1. Simplicidade de implementação

**JWT vs Keycloak:**

* JWT com chave própria:

    * Implementação direta na aplicação
    * Sem dependência de serviços externos
* Keycloak:

    * Requer configuração de servidor dedicado
    * Introduz complexidade operacional

**Impacto:**

* Redução de tempo de desenvolvimento
* Menor esforço operacional no MVP

---

### 2. Baixo acoplamento externo

**JWT vs Keycloak:**

* JWT:

    * Autenticação totalmente controlada pela aplicação
* Keycloak:

    * Dependência de infraestrutura externa (IdP)

**Impacto:**

* Maior autonomia do sistema
* Menor risco operacional inicial

---

### 3. Uso de criptografia assimétrica

A utilização de **chaves assimétricas** (RSA) permite:

* Assinatura com **private key**
* Validação com **public key**
* Separação clara de responsabilidades

**Impacto:**

* Maior segurança comparado a chave simétrica
* Possibilidade de validação distribuída de tokens
* Preparação para cenários futuros com múltiplos serviços

---

### 4. Adequação ao contexto do MVP

O sistema atual:

* Possui número reduzido de usuários
* Não exige federação de identidade
* Não possui múltiplos domínios de autenticação

**Impacto:**

* Solução mais simples atende plenamente os requisitos atuais
* Evita overengineering

---

### 5. Controle direto sobre autenticação

* Controle total sobre:

    * Geração de tokens
    * Claims
    * Expiração
    * Regras de autenticação

**Impacto:**

* Flexibilidade na implementação
* Ajuste fino conforme necessidade do domínio

---

## Alternatives Considered

### Keycloak

**Prós:**

* Solução completa de Identity and Access Management (IAM)
* Suporte a:

    * SSO (Single Sign-On)
    * OAuth2 / OpenID Connect
    * Gestão de usuários e roles
* Integração nativa com Quarkus
* Segurança consolidada

**Contras:**

* Complexidade operacional elevada
* Necessidade de infraestrutura adicional
* Overhead para MVP
* Curva de aprendizado

---

## Consequences

### Positivas

* Implementação simples e rápida
* Baixo custo operacional
* Independência de serviços externos
* Uso de padrão amplamente adotado (JWT)
* Boa segurança com criptografia assimétrica

---

### Negativas

* Responsabilidade da segurança fica na aplicação
* Ausência de recursos avançados de IAM (SSO, gestão centralizada)
* Maior esforço futuro para escalar autenticação
* Necessidade de implementação manual de algumas funcionalidades (refresh token, revogação)

---

## Evolution Strategy

A estratégia de autenticação foi definida considerando evolução futura.

### Cenários que podem justificar migração para Keycloak:

* Necessidade de Single Sign-On (SSO)
* Integração com múltiplas aplicações
* Crescimento da base de usuários
* Necessidade de gestão centralizada de identidade
* Integração com provedores externos (Google, LDAP, etc.)

---

### Estratégia de migração:

* Introdução gradual de um Identity Provider (Keycloak)
* Externalização da autenticação
* Uso de protocolos padrão (OAuth2 / OpenID Connect)
* Substituição progressiva da emissão de tokens

---

### Preparação arquitetural:

* Uso de JWT já compatível com padrões de mercado
* Estrutura desacoplada da lógica de autenticação
* Possibilidade de validação externa de tokens

---

## Scope

Esta decisão se aplica a:

* Autenticação de usuários da API
* Proteção de endpoints administrativos
* Geração e validação de tokens

---

## References

* JSON Web Token (JWT) RFC 7519
* Conceitos de criptografia assimétrica (RSA)
* OAuth2 e OpenID Connect
* Documentação do Keycloak
