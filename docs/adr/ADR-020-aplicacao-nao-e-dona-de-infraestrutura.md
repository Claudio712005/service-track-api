# ADR – 020: A aplicação não é dona de infraestrutura

## Data
27/07/2026

---

## Status

- Aceita

Origem: `GLOBAL-RFC-005`.

---

## Contexto

Nas Fases 1 e 2 este repositório concentrava tudo: aplicação, Terraform, manifestos
Kubernetes, ArgoCD e scripts operacionais. Fazia sentido enquanto havia um repositório só.

A Fase 3 exige quatro repositórios separados, e a infraestrutura viva passou a ser
`service-track-aws-iac`, com dois ambientes isolados. O `infra/` daqui continuou existindo,
descrevendo um cluster `servicetrack-dev` e um ECR `service-track-app` que não existem mais.

Duas descrições divergentes da mesma infraestrutura, nenhuma marcada como obsoleta. O risco
concreto era aplicar a stack errada, ou o ArgoCD apontar para o repositório errado.

A esteira de CD tinha o mesmo problema: construía para o ECR antigo e fazia bump do próprio
overlay, enquanto `IAC-ADR-015` definia entrega por `repository_dispatch` — que nunca foi
implementada deste lado.

---

## Decisão

**Este repositório contém apenas a aplicação.** Foram removidos `infra/` (Terraform, Kustomize,
ArgoCD, kind), `scripts/` e as esteiras `infra.yml` e `bootstrap-prod.yml`.

A esteira de CD passa a **publicar a imagem e delegar o deploy**:

1. constrói e publica em `servicetrack-<env>-app`, com a tag igual ao commit SHA;
2. aplica o portão de vulnerabilidade — falha se o scan do ECR apontar `CRITICAL`;
3. dispara `repository_dispatch` do tipo `image-published` para o repositório de
   infraestrutura, que reescreve a tag do overlay e deixa o ArgoCD sincronizar.

Push na `main` entrega em `hml`. Promoção para `prd` é manual, e pode promover uma tag já
publicada sem reconstruir a imagem.

As decisões de infraestrutura das fases anteriores — `API-ADR-015` a `API-ADR-018` — permanecem
neste repositório como registro histórico de quando foram tomadas. A **execução** delas vive em
`service-track-aws-iac`.

> **Emenda de 30/07/2026 (`GLOBAL-RFC-006`).** O parágrafo acima foi revisto. Manter o registro
> aqui e a execução lá provou-se pior do que mover: quem alterava a infraestrutura tinha de
> lembrar de atualizar um ADR em outro repositório, e não lembrava. `API-ADR-015` a
> `API-ADR-018` foram transferidos para `service-track-aws-iac` como `IAC-ADR-019` a
> `IAC-ADR-022`, com os RFCs correspondentes. A numeração 015–018 **não é reaproveitada** aqui.
> O princípio da decisão não muda — só passou a valer também para a documentação, não apenas
> para o código.

---

## Consequências

### Positivas
- Uma única descrição da infraestrutura; some o risco de aplicar a stack errada.
- Fronteira de responsabilidade explícita: quem mexe em domínio não mexe em cluster.
- O portão de vulnerabilidade fica onde `IAC-ADR-016` determina — o repositório de
  infraestrutura só recebe tag aprovada.
- A esteira de CD deixa de precisar de acesso ao cluster.

### Negativas
- Subir um ambiente do zero passa a exigir navegar por três repositórios.
- O CD depende do secret `IAC_REPO_TOKEN`. Se vazar, o dano máximo é um commit de bump,
  revertível — mas é mais um segredo a manter.
- Desenvolvimento local em Kubernetes deixa de ter manifestos aqui; usar os do repositório de
  infraestrutura.
- Links da documentação das Fases 1 e 2 que apontavam para `infra/` deixam de resolver em
  `main`. O conteúdo continua acessível pelo histórico.

### Impacto em ambiente efêmero
Positivo. Havia dois caminhos possíveis para subir um ambiente, e o ciclo de recriação é
semanal — ambiguidade em procedimento repetido é onde os erros acontecem. A transição foi
feita sem janela sem CD: o `repository_dispatch` entrou no mesmo conjunto de mudanças em que o
fluxo antigo saiu.

---

## Alternativas Consideradas

### Opção 1: Manter com aviso de obsolescência
Um `DEPRECATED.md` e avisos no README.
Rejeitada: não elimina o risco de alguém aplicar a stack errada, e documentação errada ativa é
pior que documentação ausente.

### Opção 2: Preservar em tag `fase-2` antes de remover
Rejeitada como passo extra: o histórico do Git já preserva tudo, e as branches `fase-1` e
`fase-2` continuam existindo.
