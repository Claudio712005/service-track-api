# ADR-006 — Integração com API FIPE

**Status:** Aceito  
**Data:** 2026-05-10  
**Autores:** Equipe ServiceTrack

---

## Contexto

O sistema de gestão de oficinas precisa garantir que os veículos cadastrados correspondam a marcas reais do mercado nacional. A tabela FIPE é a referência oficial de preços e dados de veículos no Brasil, amplamente conhecida e mantida mensalmente.

Sem validação de marca, o sistema poderia acumular dados inconsistentes (ex.: "Toyotra", "HONDA", "fiat") que prejudicariam relatórios, buscas e integrações futuras.

---

## Decisão

Integrar a API FIPE (https://fipe.parallelum.com.br/api/v2) para:

1. **Validar a marca** informada ao cadastrar ou atualizar um veículo — impedindo cadastro de marcas inexistentes na tabela FIPE.
2. **Expor consultas** de marcas, modelos, anos e detalhes de veículos como endpoints de apoio.

### Endpoints consumidos

| Endpoint FIPE | Finalidade |
|---|---|
| `GET /cars/brands` | Listar marcas de automóveis |
| `GET /cars/brands/{id}/models` | Listar modelos de uma marca |
| `GET /cars/brands/{id}/models/{id}/years` | Listar anos de um modelo |
| `GET /cars/brands/{id}/models/{id}/years/{ano}` | Consultar detalhes e preço FIPE |

### Arquitetura adotada

- **Porta de saída** `FipePort` definida na camada de aplicação — isolamento total do domínio.
- **Adapter** `FipeClientAdapter` na infraestrutura implementa a porta usando MicroProfile REST Client.
- **DTOs externos** (`FipeMarcaResponse`, `FipeDetalheResponse` etc.) ficam na camada de infraestrutura — o domínio nunca os vê.
- **Cache** via `quarkus-cache` (Caffeine) com TTL de 1 hora para listagem de marcas e modelos — dados que raramente mudam.

### Estratégia de falha

- Se a API FIPE estiver indisponível durante cadastro/atualização de veículo, a operação é **rejeitada** (`503 Service Unavailable`) com mensagem clara.
- A validação é obrigatória e não pode ser ignorada silenciosamente, pois comprometeria a integridade dos dados.

---

## Consequências

**Positivas:**
- Dados de marca padronizados e confiáveis em toda a base.
- Possibilidade futura de enriquecer veículos com preço FIPE automaticamente.
- Baixo acoplamento: trocar a fonte de dados de veículos requer apenas um novo adapter.

**Negativas:**
- Dependência de serviço externo gratuito (limite de 500 req/dia sem token).
- Cadastro de veículo falha se a FIPE estiver fora do ar (mitigado pelo cache).
- Marcas novas ou regionais inexistentes na FIPE não podem ser cadastradas.

---

## Alternativas consideradas

| Alternativa | Motivo da rejeição |
|---|---|
| Manter lista estática de marcas no código | Desatualização garantida; manutenção manual |
| Sem validação de marca | Dados inconsistentes na base |
| Validação apenas em nível de UI | Não garante integridade via API direta |
