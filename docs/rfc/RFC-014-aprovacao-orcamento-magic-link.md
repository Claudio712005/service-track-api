# RFC – 014: Aprovação/Reprovação de Orçamento por Magic Link no E-mail

## Data
04/07/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para permitir que o cliente aprove ou recuse o orçamento com **um clique em um link do e-mail** (magic link), autorizado por um **token dedicado** e exclusivo do dono da OS, atendendo ao requisito de "atualização de status da OS via alguma ferramenta como e-mail".

---

## Problema

A aprovação/reprovação de orçamento só existia via endpoints autenticados (`POST .../orcamento/aprovacao|reprovacao`, `@RolesAllowed("CLIENTE")`), exigindo login no app. O desafio pede que a atualização de status seja disparada **por uma ferramenta como e-mail** — ou seja, uma ação iniciada a partir do próprio e-mail, sem depender de o cliente acessar o app.

Complicações:

- Não há frontend/DNS/domínio fixo (conta de estudante) — o link precisa apontar para o caminho público dinâmico da API.
- Um endpoint público de decisão não pode usar o token de autenticação normal; precisa de uma credencial de escopo mínimo.
- É preciso garantir que apenas o **dono** da OS decida e que o link não seja reutilizável após a decisão.

---

## Proposta Técnica

**Magic link** com token dedicado:

- **Token isolado** (`TokenDecisaoOrcamentoPort`): JWT assinado com a mesma chave RSA, mas com `purpose=DECISAO_ORCAMENTO`, `audience=orcamento-decisao`, `sub=clienteId`, `osId` e expiração de 72h. Não carrega grupos/roles → não acessa nenhum endpoint autenticado.
- **Endpoints públicos** (`@PermitAll`, `GET`), com o `osId` embutido no token:
  - `GET /ordem-servico/orcamento/aprovacao?token=...`
  - `GET /ordem-servico/orcamento/reprovacao?token=...`
  - Validam token → carregam OS → conferem `os.clienteId == token.clienteId` → decidem → devolvem **página HTML** de confirmação (renderizada via Qute, sem frontend).
- **Uso único** garantido pelo domínio: após decidir, a OS sai de `AGUARDANDO_APROVACAO`; um segundo clique cai em transição inválida → página de erro. Sem store de revogação.
- **Reprovação em um clique**: motivo padrão ("Reprovado pelo cliente via e-mail").
- **E-mails**:
  - Ao entrar em `AGUARDANDO_APROVACAO`, além do e-mail genérico de status, é enviado um **e-mail adicional** ao cliente com botões Aprovar/Recusar (`SOLICITACAO_APROVACAO_ORCAMENTO_OS`).
  - Após a decisão, o **mecânico** recebe um e-mail com o resultado (`DECISAO_ORCAMENTO_OS`).
- **Anti-duplicação**: a decisão (bearer e magic link) compartilha o colaborador `DecididorOrcamento` (reposição de estoque na reprovação, transição, persistência, evento de status e notificação do mecânico). O token→OS+dono é resolvido por `ResolvedorOrdemServicoPorToken`.
- **Caminho público da API**: `servicetrack.api.base-url` (env `SERVICETRACK_API_BASE_URL`), preenchido no deploy a partir do hostname do LoadBalancer (capturado em `deploy-k8s.sh`, já que não há IP fixo/DNS).
- **Contract-first**: endpoints definidos no OpenAPI; interface JAX-RS gerada e implementada no resource.

---

## Alternativas Consideradas

### Opção 1: Webhook de pagamento (Mercado Pago / PIX)
- Pagamento aprovado dispara a transição.
- Prós: integração externa realista.
- Contras: conta MP, validação de assinatura, idempotência, túnel público em dev; escopo maior. Fica como evolução futura.

### Opção 2: Reply de e-mail parseado (IMAP)
- Cliente responde "APROVAR"; worker lê a caixa.
- Prós: sem link.
- Contras: frágil, spoofável, sem confirmação clara. Descartado.

---

## Pontos em Aberto

- Reprovação com formulário de motivo (hoje motivo padrão) — pode virar página com campo.
- Webhook Mercado Pago como segundo gatilho da mesma transição.

---

## Impactos

### Positivos
- Atende o requisito "atualização via e-mail" de forma literal (clique → ação na API).
- Reaproveita domínio, decisor, auditoria e pipeline de notificação.
- Token de escopo mínimo; sem sessão, sem frontend.

### Negativos
- Endpoints públicos exigem cuidado (token assinado, escopo, expiração).
- Depende do caminho público dinâmico da API preenchido no deploy.

---

## Próximos Passos
- Revisão pelo time
- (Aprovado) geração do ADR correspondente → [ADR-014](../adr/ADR-014-aprovacao-orcamento-magic-link.md)
