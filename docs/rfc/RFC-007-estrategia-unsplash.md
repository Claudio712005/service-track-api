# RFC-007 — Estratégia de Integração com a API Unsplash

**Status:** Implementado (ver ADR-007)  
**Data:** 2026-05-10

---

## Problema

Como oferecer sugestões de imagens relevantes para veículos cadastrados, sem armazenar arquivos binários no sistema?

## Proposta

Expor o endpoint `GET /veiculos/imagens/sugestoes?marca=X&modelo=Y` que consulta a API Unsplash e retorna até 10 URLs de imagens.

## Fluxo

```
Cliente → GET /veiculos/imagens/sugestoes?marca=Toyota&modelo=Corolla
       → BuscarSugestoesImagensService.buscarSugestoes("Toyota", "Corolla")
       → UnsplashPort.buscarImagensVeiculo(marca, modelo, 10)  ← cache 30min
       → UnsplashClientAdapter → GET /search/photos?query=Toyota Corolla carro
       → Retorna lista de URLs (campo `urls.regular` de cada foto)
```

## Formato de resposta

```json
{
  "imagens": [
    "https://images.unsplash.com/photo-abc...",
    "https://images.unsplash.com/photo-def..."
  ]
}
```

## Considerações

- Se a chave de acesso (`UNSPLASH_CHAVE_ACESSO`) não estiver configurada, retorna lista vazia com log de aviso — **fail-open**.
- Se a API falhar, retorna lista vazia com log de aviso — nunca quebra a experiência do usuário.
- O campo `urlImagem` do veículo é preenchido separadamente pelo usuário (via PUT /veiculos/{id}) após escolher uma das sugestões.
- Apenas URLs `regular` (tamanho padrão) são retornadas — nem `full` (muito pesada) nem `small` (baixa qualidade).
