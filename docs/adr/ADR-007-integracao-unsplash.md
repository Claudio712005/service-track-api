# ADR-007 — Integração com API Unsplash

**Status:** Aceito  
**Data:** 2026-05-10  
**Autores:** Equipe ServiceTrack

---

## Contexto

Mecânicos e clientes precisam associar imagens aos veículos cadastrados. Permitir que o usuário informe manualmente uma URL de imagem é funcional, mas pouco amigável. Uma forma de enriquecer a experiência é sugerir imagens relevantes ao veículo com base na marca e modelo.

O Unsplash é um banco de imagens de alta qualidade, gratuito para uso com atribuição via API, e amplamente utilizado para esse tipo de integração.

---

## Decisão

Integrar a API Unsplash (https://api.unsplash.com) para:

1. **Buscar sugestões de imagens** com base em marca e modelo do veículo.
2. **Expor o endpoint** `GET /veiculos/imagens/sugestoes?marca=X&modelo=Y` que retorna uma lista de URLs.

### Endpoint consumido

```
GET /search/photos?query={marca} {modelo} carro&per_page=10
Authorization: Client-ID {chave-acesso}
```

### Arquitetura adotada

- **Porta de saída** `UnsplashPort` definida na camada de aplicação.
- **Adapter** `UnsplashClientAdapter` na infraestrutura implementa a porta via MicroProfile REST Client.
- **DTOs externos** (`UnsplashBuscaResponse`, `UnsplashFotoResponse`) isolados na camada de infraestrutura.
- **Cache** via `quarkus-cache` com TTL de 30 minutos — evita re-consultas repetidas para mesma marca/modelo.
- **Chave de acesso** configurada via variável de ambiente `UNSPLASH_CHAVE_ACESSO`.

### Estratégia de falha (fail-open)

- Se a API Unsplash estiver indisponível ou a chave não estiver configurada, o endpoint **retorna lista vazia** sem lançar exceção.
- Imagens são sugestões, não requisito funcional — a operação de cadastro de veículo é independente.
- Apenas URLs são retornadas — nenhuma imagem binária é armazenada no sistema.

---

## Consequências

**Positivas:**
- Enriquecimento de UX sem dependência crítica.
- Zero armazenamento de imagens (apenas URLs).
- Fácil substituição por outro banco de imagens (Getty, Pexels etc.) via troca de adapter.

**Negativas:**
- Requer chave de API Unsplash para produção (cadastro gratuito).
- Limite de 50 req/hora no plano gratuito do Unsplash (mitigado pelo cache).
- Imagens podem não corresponder exatamente ao modelo específico do ano informado.

---

## Alternativas consideradas

| Alternativa | Motivo da rejeição |
|---|---|
| Upload direto de imagem pelo usuário | Requer armazenamento de arquivo (S3 etc.), fora do escopo do MVP |
| Google Images API | Paga e com restrições de uso comercial |
| Sem sugestão de imagens | Válido, mas perde oportunidade de UX sem custo |
