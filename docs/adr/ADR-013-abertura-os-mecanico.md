# ADR – 013: Abertura de OS por Mecânico com Serviços e Insumos

## Data
04/07/2026

---

## Status

- Aceita

---

## Contexto

O desafio pede que a abertura de Ordem de Serviço "receba os dados do cliente, veículo, serviços e peças, retornando a identificação única da OS". A implementação anterior expunha um único endpoint de abertura (`POST /ordem-servico`) que recebia apenas cliente, mecânico e veículo; serviços e insumos eram associados depois, em `PUT /ordem-servico/{id}/diagnostico/itens`.

Fatores que motivaram a decisão:

- **Regra de negócio**: o cliente não conhece serviços e peças ao abrir a OS — o diagnóstico (e portanto a definição de serviços/insumos) é responsabilidade do **mecânico**.
- Acoplar serviços/peças ao endpoint do cliente contraria o fluxo real da oficina.
- Manter só a associação posterior deixa a "abertura com itens" sem endpoint dedicado, divergindo do texto do desafio.
- Restrição arquitetural: arquitetura hexagonal e domínio estável — evitar alteração de domínio e duplicação de código na aplicação.

Análise detalhada e alternativas em [RFC-013](../rfc/RFC-013-abertura-os-mecanico.md).

---

## Decisão

Adotar **dois caminhos de abertura de OS**, segregados por ator:

1. `POST /ordem-servico` (cliente ou mecânico) — payload enxuto (motivo, cliente, mecânico, veículo); OS nasce em `RECEBIDA`.
2. `POST /ordem-servico/completa` (`@RolesAllowed("MECANICO")`) — payload com serviços e insumos diagnosticados; OS nasce em `EM_DIAGNOSTICO`. O mecânico vinculado é o próprio solicitante autenticado (JWT).

Diretrizes de implementação:

- **Status inicial `EM_DIAGNOSTICO`**: reusa transições existentes (`abrir` → `iniciarDiagnostico` → anexa itens); nenhum caminho novo de status. O orçamento continua em endpoint próprio (`POST /{id}/orcamento`).
- **Anti-duplicação**: regras comuns extraídas para colaboradores de aplicação reutilizados:
  - `AbridorOrdemServico` — validações de abertura + construção do agregado (compartilhado com `CriarOrdemServicoService`).
  - `AssociadorItensOrdemServico` — resolução de catálogo, validação de estoque e anexação de itens (compartilhado com `AssociarItensOrdemServicoService`).
- **Auditoria** via `AuditoriaProxy` + `@Auditavel(ORDEM_SERVICO, CRIADO)`, idêntico aos demais casos.
- **Notificação por e-mail** disparada pela transição para `EM_DIAGNOSTICO` (`OrdemServicoStatusAlteradoEvent`), aderente a [ADR-009](ADR-009-notificacoes-email.md) e [ADR-010](ADR-010-cdi-events-outbox.md).
- **Contract-first**: endpoint e schema definidos no OpenAPI; interface JAX-RS gerada e implementada no resource.
- **Domínio inalterado**.

---

## Consequências

### Positivas

- Contrato alinhado ao domínio real (segregação cliente vs mecânico) e ao texto do desafio.
- Reaproveitamento de transições de status, colaboradores, auditoria e notificação existentes — sem inventar fluxo paralelo.
- Zero alteração de domínio; mudanças confinadas às camadas de aplicação e infraestrutura.
- Regras de abertura e de associação de itens passam a ter fonte única (colaboradores), reduzindo duplicação.

### Negativas

- Um endpoint e um use case adicionais a manter.
- Refatoração dos dois serviços existentes para delegar aos colaboradores (mitigado por testes unitários e de integração).
- Pequeno ajuste de infraestrutura: `OrdemServicoEntity.de()` passou a mapear `itensServico` para que `salvar()` persista o agregado completo na abertura completa.

---

## Alternativas Consideradas

### Opção 1: Endpoint único com serviços/insumos opcionais
- Um só `POST /ordem-servico` com itens opcionais por role.
- Prós: mínima mudança; contrato único.
- Contras: DTO condicional por role; validação ramificada; mistura duas intenções de negócio.

### Opção 2: Manter apenas associação posterior + documentar
- Abertura enxuta; itens só via `.../diagnostico/itens`.
- Prós: zero código.
- Contras: sem endpoint dedicado de "abertura com itens"; risco de ser lido como requisito não atendido.

---

## Referências

- [RFC-013 — Abertura de OS por Mecânico](../rfc/RFC-013-abertura-os-mecanico.md)
- [ADR-009 — Notificações por E-mail](ADR-009-notificacoes-email.md)
- [ADR-010 — CDI Events com semântica de Outbox](ADR-010-cdi-events-outbox.md)
- Diagrama C4 (code): `docs/c4/code-diagram/ordem-servico/criar-ordem-servico-completa.mmd`
- SRS — RF03 (Abertura de Ordem de Serviço)
