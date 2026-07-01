# RFC-008 — Estratégia de Client HTTP para Integrações Externas

**Status:** Implementado (ver ADR-008)  
**Data:** 2026-05-10

---

## Problema

Qual biblioteca de cliente HTTP usar para consumir APIs externas (FIPE, Unsplash) de forma consistente com o ecossistema Quarkus já adotado no projeto?

## Proposta

Usar **MicroProfile REST Client** (`quarkus-rest-client-reactive-jackson`) como padrão para todas as integrações HTTP externas do projeto.

## Padrão de implementação

```
openApi/     → contrato externo (referência, não geração)
domain/      → nenhum conhecimento de HTTP
application/ → Porta de saída (interface pura)
             → DTOs de aplicação (dados necessários para o domínio)
infrastructure/
  client/
    {servico}/
      {Servico}Client.kt          ← interface MicroProfile REST Client
      {Servico}ClientAdapter.kt   ← implementação da porta
      dto/                        ← DTOs externos (JSON ↔ DTO)
```

## Configuração por ambiente

```properties
# application.properties
quarkus.rest-client.{servico}-api.url=https://...
quarkus.rest-client.{servico}-api.connect-timeout=5000
quarkus.rest-client.{servico}-api.read-timeout=10000

# test/application.properties
%test.quarkus.rest-client.{servico}-api.url=http://localhost:9999
```

## Testes

- Adapters externos são **mockados via `@Mock` CDI** nos testes de integração.
- Os clientes REST reais **nunca são chamados** nos testes — sem dependência de internet.
- Testes unitários dos serviços usam `mockk<FipePort>()` diretamente.
