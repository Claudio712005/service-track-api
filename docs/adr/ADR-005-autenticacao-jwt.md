# ADR – 005: Estratégia de Autenticação — JWT com Chaves Assimétricas

## Data
18/04/2026

---

## Status

- Aceita

---

## Contexto

O sistema MVP requer autenticação de usuários, proteção de endpoints REST e controle de acesso baseado em perfil (RBAC). Não há necessidade de SSO, federação de identidade ou integração com múltiplos provedores. O estágio atual favorece soluções simples, com baixo custo operacional e sem dependências de infraestrutura externa.

---

## Decisão

Adotar **JWT (JSON Web Token) com criptografia assimétrica RSA** como mecanismo de autenticação.

Características:
- Tokens assinados com **private key** e validados com **public key**
- Implementação direta na aplicação, sem dependência de serviços externos
- Controle total sobre claims, expiração e regras de autenticação
- Padrão de mercado (RFC 7519), compatível com migração futura para Identity Provider

---

## Consequências

### Positivas
- Implementação simples, rápida e totalmente sob controle da aplicação
- Independência de infraestrutura externa; sem ponto único de falha externo
- Segurança superior à chave simétrica — separação clara de responsabilidades sign/verify
- Padrão amplamente adotado com amplo suporte em bibliotecas e frameworks

### Negativas
- Responsabilidade integral da segurança centralizada na aplicação
- Ausência de recursos avançados de IAM: SSO, revogação centralizada, gestão de sessões
- Maior esforço futuro para escalar autenticação com múltiplas aplicações
- Funcionalidades como refresh token e revogação de tokens requerem implementação manual

---

## Alternativas Consideradas

### Opção 1: Keycloak
- Identity Provider completo com suporte a OAuth2 e OpenID Connect
- Prós: SSO nativo, gestão centralizada de usuários e roles, integração nativa com Quarkus, segurança consolidada e auditada
- Contras: exige infraestrutura dedicada adicional, complexidade operacional elevada, custo de configuração e manutenção incompatível com o estágio MVP

---

## Referências

- [RFC-005 – Estratégia de Autenticação com JWT Assimétrico](../rfc/RFC-005-autenticacao-jwt.md)
- JWT — RFC 7519 (IETF)
- Criptografia assimétrica RSA — PKCS#1
- OAuth2 — RFC 6749 e OpenID Connect Core 1.0
- Documentação do Keycloak — keycloak.org
