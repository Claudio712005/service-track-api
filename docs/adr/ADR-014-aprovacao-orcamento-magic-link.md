# ADR – 014: Decisão de Orçamento por Magic Link com Token Dedicado

## Data
04/07/2026

---

## Status

- Aceita

---

## Contexto

O desafio pede "atualização de status da OS via alguma ferramenta como e-mail". A aprovação/reprovação de orçamento existia apenas via endpoints autenticados no app. Precisávamos de uma ação disparada a partir do próprio e-mail, sem frontend nem DNS/domínio fixo (conta de estudante, IP dinâmico), garantindo que apenas o dono da OS decida e que o link não seja reutilizável.

Análise e alternativas em [RFC-014](../rfc/RFC-014-aprovacao-orcamento-magic-link.md).

---

## Decisão

Adotar **magic link** com **token dedicado**:

- **Token isolado do de autenticação** (`TokenDecisaoOrcamentoPort` → adapter SmallRye JWT): `purpose=DECISAO_ORCAMENTO`, `audience=orcamento-decisao`, `sub=clienteId`, claim `osId`, expiração 72h; sem grupos/roles (não acessa endpoints autenticados).
- **Endpoints públicos** `@PermitAll` `GET /ordem-servico/orcamento/{aprovacao|reprovacao}?token=...`, com `osId` no token; validam token, conferem dono (`os.clienteId == token.clienteId`) e retornam **página HTML** (Qute).
- **Uso único pelo domínio**: após decidir, a OS deixa `AGUARDANDO_APROVACAO`; novo clique cai em transição inválida → página de erro. Sem store de revogação.
- **Reprovação em um clique** com motivo padrão.
- **E-mails**: e-mail adicional ao cliente com botões ao entrar em `AGUARDANDO_APROVACAO` (`SOLICITACAO_APROVACAO_ORCAMENTO_OS`); e-mail ao mecânico após a decisão (`DECISAO_ORCAMENTO_OS`).
- **Fonte única de decisão** (`DecididorOrcamento`), compartilhada entre o canal autenticado e o magic link — inclui reposição de estoque na reprovação, transição, persistência, evento de status e notificação do mecânico. Token→OS+dono via `ResolvedorOrdemServicoPorToken`.
- **Caminho público da API** por configuração (`servicetrack.api.base-url` / env `SERVICETRACK_API_BASE_URL`), preenchido no deploy a partir do hostname do LoadBalancer (capturado em `deploy-k8s.sh`).
- **Contract-first**; **domínio inalterado** (reusa `aprovarOrcamento`/`reprovarOrcamento`).

---

## Consequências

### Positivas
- Atende o requisito de atualização de status via e-mail de forma literal e demonstrável.
- Token de escopo mínimo, sem sessão nem frontend; seguro por assinatura + purpose/audience + expiração.
- Reaproveita domínio, decisor, auditoria e pipeline assíncrono de notificação; mecânico passa a ser notificado em ambos os canais.

### Negativas
- Endpoints públicos ampliam a superfície — mitigado por token assinado, escopo e expiração.
- Depende do caminho público dinâmico preenchido no deploy (sem DNS/IP fixo).
- Reprovação em um clique usa motivo padrão (sem detalhamento do cliente).

---

## Alternativas Consideradas

### Opção 1: Webhook de pagamento (Mercado Pago / PIX)
- Prós: integração externa realista. Contras: conta MP, assinatura, idempotência, túnel em dev. Evolução futura.

### Opção 2: Reply de e-mail parseado (IMAP)
- Prós: sem link. Contras: frágil, spoofável. Descartado.

---

## Referências

- [RFC-014 — Aprovação de Orçamento por Magic Link](../rfc/RFC-014-aprovacao-orcamento-magic-link.md)
- [ADR-009 — Notificações por E-mail](ADR-009-notificacoes-email.md)
- [ADR-005 — Autenticação JWT](ADR-005-autenticacao-jwt.md)
- Diagrama C4 (code): `docs/c4/code-diagram/ordem-servico/decidir-orcamento-via-email.mmd`
- SRS — RF07 (Autorização de Serviço)
