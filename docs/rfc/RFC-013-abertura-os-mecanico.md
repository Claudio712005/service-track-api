# RFC – 013: Abertura de OS por Mecânico (dois caminhos de abertura)

## Data
04/07/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para criar um segundo endpoint de abertura de Ordem de Serviço, exclusivo do mecânico, que recebe já os serviços e insumos diagnosticados e abre a OS diretamente em `EM_DIAGNOSTICO` — sem acoplar essa responsabilidade ao cliente.

---

## Problema

O endpoint único de abertura (`POST /ordem-servico`) recebe apenas cliente, mecânico e veículo; serviços e insumos só entram depois, via `PUT /ordem-servico/{id}/diagnostico/itens`.

O desafio pede que a abertura de OS "receba os dados do cliente, veículo, serviços e peças". Contudo:

- O **cliente não conhece** os serviços e peças no momento da abertura — quem diagnostica o veículo e define serviços/insumos é o **mecânico**.
- Forçar serviços/peças no endpoint único do cliente contraria a regra de negócio e o fluxo real da oficina.
- Manter apenas a associação posterior deixa a abertura "com itens" sem endpoint dedicado, divergindo do texto do desafio.

---

## Proposta Técnica

Adotar **dois caminhos de abertura**, um por ator:

| Rota | Ator | Payload | Status inicial |
|---|---|---|---|
| `POST /ordem-servico` | Cliente (ou mecânico) | motivo, cliente, mecânico, veículo | `RECEBIDA` |
| `POST /ordem-servico/completa` | Mecânico | motivo, cliente, veículo, **serviços + insumos** | `EM_DIAGNOSTICO` |

Detalhes:

- **Autorização**: `POST /ordem-servico/completa` é `@RolesAllowed("MECANICO")`; o mecânico vinculado é o próprio solicitante autenticado (extraído do JWT), não vem no payload.
- **Status inicial `EM_DIAGNOSTICO`**: o mecânico já inspecionou o veículo ao abrir a OS com itens; portanto ela nasce em diagnóstico. Internamente reusa as transições existentes: `abrir()` (RECEBIDA) → `iniciarDiagnostico()` (EM_DIAGNOSTICO) → anexa itens. Nenhum caminho novo de status é introduzido.
- **Orçamento permanece separado**: após a abertura completa, o mecânico usa `POST /ordem-servico/{id}/orcamento` (fluxo já existente) → `AGUARDANDO_APROVACAO` → cliente aprova.
- **Anti-duplicação (hexagonal)**: as regras comuns são extraídas para dois colaboradores de aplicação reutilizados pelos serviços existentes e pelo novo:
  - `AbridorOrdemServico` — valida cliente/mecânico/veículo e OS aberta duplicada, constrói o agregado. Usado por `CriarOrdemServicoService` e `CriarOrdemServicoCompletaService`.
  - `AssociadorItensOrdemServico` — resolve serviços/insumos do catálogo, valida estoque e anexa itens. Usado por `AssociarItensOrdemServicoService` e `CriarOrdemServicoCompletaService`.
- **Auditoria**: `CriarOrdemServicoCompletaService` é anotado `@Auditavel(ORDEM_SERVICO, CRIADO)` e envolvido pelo `AuditoriaProxy`, idêntico aos demais casos.
- **Notificação**: a transição para `EM_DIAGNOSTICO` dispara `OrdemServicoStatusAlteradoEvent`, notificando o cliente por e-mail (consistente com os demais eventos de status — ADR-009/ADR-010).
- **Domínio inalterado**: nenhuma regra de domínio foi alterada; o fluxo reusa métodos existentes do agregado.
- **Contract-first**: o endpoint é definido no OpenAPI (`ordemServico_completa.yaml` + `CriarOrdemServicoCompletaRequest.yaml`); a interface JAX-RS é gerada e implementada no resource.

---

## Alternativas Consideradas

### Opção 1: Endpoint único com serviços/insumos opcionais

- `POST /ordem-servico` aceita `servicos`/`insumos` opcionais; cliente omite, mecânico envia.
- Prós: satisfaz o texto do desafio com mínima mudança; um só contrato.
- Contras: DTO com campos condicionais por role; validação ramificada; contrato menos explícito; mistura duas intenções de negócio distintas.

### Opção 2: Manter apenas a associação posterior + documentar

- Deixa a abertura enxuta e itens só via `.../diagnostico/itens`.
- Prós: zero código; modelo de domínio "itens surgem no diagnóstico" defensável.
- Contras: avaliador ao pé da letra pode marcar "abertura recebe serviços e peças" como não atendido; sem endpoint dedicado.

---

## Pontos em Aberto

- Abrir a OS completa já em `AGUARDANDO_APROVACAO` (gerando o orçamento no mesmo passo) — descartado por acoplar custo de mão de obra ao payload de abertura; orçamento segue endpoint próprio.
- Permitir que o mecânico abra a OS para outro mecânico responsável — hoje o mecânico vinculado é sempre o solicitante.

---

## Impactos

### Positivos
- Alinha o contrato ao domínio real (cliente vs mecânico) e ao texto do desafio.
- Reaproveita transições, colaboradores, auditoria e notificação existentes.
- Zero alteração de domínio; blast radius limitado à aplicação/infra.

### Negativos
- Mais um endpoint e um use case a manter.
- Refatoração dos dois serviços existentes para delegar aos colaboradores (coberta por testes).

---

## Próximos Passos
- Revisão pelo time
- (Aprovado) geração do ADR correspondente → [ADR-013](../adr/ADR-013-abertura-os-mecanico.md)
