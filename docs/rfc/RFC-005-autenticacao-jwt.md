# RFC – 005: Estratégia de Autenticação com JWT Assimétrico

## Data
18/04/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para adoção de JWT (JSON Web Token) com criptografia assimétrica RSA como mecanismo de autenticação do sistema de gestão de ordens de serviço, com objetivo de proteger endpoints REST com controle de acesso por perfil (RBAC), sem dependência de infraestrutura externa, mantendo compatibilidade com migração futura para Identity Provider.

---

## Problema

O sistema exige controle de acesso diferenciado por perfil (cliente vs. mecânico vs. administrador) sobre endpoints REST. Os desafios são:

- Autenticação stateless: o sistema não deve manter sessão server-side para cada requisição
- Controle de acesso granular: endpoints devem ser acessíveis apenas por perfis autorizados
- Segurança do token: o mecanismo deve impedir adulteração e forjamento de identidade
- Baixo acoplamento externo: o MVP não deve depender de serviços de identidade externos para funcionar
- Compatibilidade futura: a solução deve permitir migração gradual para um Identity Provider (ex.: Keycloak) sem reescrita da lógica de autorização

---

## Proposta Técnica

### Mecanismo escolhido: JWT com RSA (RS256)

#### Estrutura do token JWT

```
Header:  { "alg": "RS256", "typ": "JWT" }
Payload: {
  "sub":    "<usuarioId>",       // identificador único do usuário
  "perfil": "CLIENTE|MECANICO",  // perfil para RBAC
  "iat":    <timestamp>,         // emitido em
  "exp":    <timestamp>          // expira em (ex: 1h)
}
Signature: RSA-SHA256(base64(header) + "." + base64(payload), privateKey)
```

#### Fluxo de autenticação

```
1. Cliente POST /autenticacao { email, senha }
2. Aplicação valida credenciais (senha bcrypt vs. hash armazenado)
3. Aplicação assina JWT com private key (RSA 2048 bits)
4. Token retornado ao cliente
5. Requisições subsequentes: Authorization: Bearer <token>
6. Aplicação valida assinatura com public key (sem estado server-side)
7. Claims do token populam SecurityContext para controle de acesso (@RolesAllowed)
```

#### Criptografia assimétrica RSA — motivação

| Aspecto            | Chave Simétrica (HS256) | Chave Assimétrica (RS256) |
|--------------------|-------------------------|---------------------------|
| Validação          | Mesma chave sign/verify | Public key para verify     |
| Risco de vazamento | Sign e verify expostos  | Apenas public key exposta  |
| Validação externa  | Requer compartilhar chave| Public key distribuída livremente |
| Preparação para MSc| Limitada                | Cada serviço valida com public key |

A separação entre private key (apenas na aplicação que emite) e public key (pode ser distribuída para qualquer serviço validador) prepara o sistema para um cenário futuro com múltiplos serviços ou um Identity Provider externo.

#### Gerenciamento de chaves

- Chaves RSA 2048 bits geradas com `openssl genrsa` e `openssl rsa`
- `privateKey.pem` armazenada como secret (variável de ambiente / secret management)
- `publicKey.pem` configurada em `application.properties` do Quarkus
- Em CI: geração automática de par de chaves efêmeras para testes de integração
- Em produção: rotação de chaves planejada com período de validade duplo durante transição

#### Integração com Quarkus SmallRye JWT

```kotlin
// Proteção de endpoint com perfil
@GET
@Path("/{id}")
@RolesAllowed("MECANICO", "CLIENTE")
fun buscarCliente(@PathParam("id") id: String): Response { ... }

// Acesso ao contexto de segurança no Application Service
@Inject
lateinit var jwt: JsonWebToken

val solicitanteId: String = jwt.subject
val perfil: String = jwt.getClaim("perfil")
```

#### Controle de acesso baseado em perfil (RBAC)

- `CLIENTE`: acesso apenas aos próprios dados (veículos, OS, perfil)
- `MECANICO`: acesso a dados de clientes, OS e catálogo de serviços
- Verificação de ownership em Application Services quando necessário (não apenas no nível de role)

#### Limitações e mitigações no MVP

| Limitação                     | Mitigação adotada                                      |
|-------------------------------|--------------------------------------------------------|
| Sem revogação de tokens       | Expiração curta (1h); refresh token planejado          |
| Sem refresh token             | Re-autenticação após expiração; implementação futura   |
| Gestão manual de chaves       | Documentação de procedimento de rotação; secret manager|
| Sem gestão centralizada de IAM| Escopo limitado de usuários no MVP; Keycloak futuro    |

---

## Alternativas Consideradas

### Opção 1: Keycloak

- Identity Provider completo com suporte a OAuth2 e OpenID Connect
- Prós: SSO nativo, gestão centralizada de usuários, roles e sessões; revogação de tokens; suporte a MFA; integração nativa com Quarkus via `quarkus-oidc`; auditoria de acessos out-of-the-box; compatível com LDAP/AD para cenários enterprise
- Contras: exige servidor dedicado adicional (Docker container ou instância gerenciada); configuração inicial complexa; overhead operacional incompatível com o estágio MVP; ponto de falha externo — se Keycloak cair, toda autenticação falha; curva de aprendizado elevada para configuração de realms, clientes e flows

---

### Opção 2: JWT com chave simétrica (HS256)

- Mesma chave usada para assinar e verificar tokens
- Prós: implementação mais simples, sem necessidade de par de chaves
- Contras: chave deve ser compartilhada com qualquer serviço que precise validar tokens; vazamento da chave compromete tanto a emissão quanto a validação; não preparado para cenário multi-serviço

---

## Pontos em Aberto

- Implementação de refresh token: estratégia de armazenamento (banco vs. cookie HttpOnly) e período de validade
- Revogação de tokens antes da expiração: blacklist em Redis ou redução do TTL
- Estratégia formal de rotação de chaves RSA em produção sem downtime
- Migração para Keycloak: critérios objetivos que disparam a migração (número de usuários, necessidade de SSO, integração com AD/LDAP)
- Auditoria de acessos: log de autenticações bem-sucedidas e falhas para rastreabilidade

---

## Impactos

### Positivos
- Autenticação stateless elimina overhead de sessão server-side
- Independência total de infraestrutura externa no MVP
- Criptografia assimétrica oferece segurança superior à chave simétrica
- Padrão JWT amplamente adotado facilita integração com clientes (mobile, web, terceiros)
- Arquitetura preparada para migração futura para Keycloak com mudanças mínimas

### Negativos
- Responsabilidade integral de segurança na aplicação: sem auditoria automática de acessos
- Tokens JWT não revogáveis antes da expiração sem infraestrutura adicional (blacklist)
- Gestão manual de par de chaves RSA requer processo formal de rotação e backup
- Funcionalidades avançadas (MFA, gestão de sessões, recuperação de senha) requerem implementação do zero

---

## Próximos Passos

- Revisão pelo time de segurança e backend
- Definição do TTL padrão de tokens e política de refresh
- Documentação do procedimento de geração, armazenamento e rotação de chaves RSA
- Implementação do fluxo de autenticação e validação de tokens com SmallRye JWT
- Aprovação formal e geração da ADR-005 correspondente
