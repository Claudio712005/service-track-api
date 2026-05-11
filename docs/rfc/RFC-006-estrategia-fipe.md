# RFC-006 — Estratégia de Integração com a API FIPE

**Status:** Implementado (ver ADR-006)  
**Data:** 2026-05-10

---

## Problema

Como validar que a marca de um veículo cadastrado na plataforma corresponde a uma marca real do mercado, sem manter uma lista estática sujeita a desatualização?

## Proposta

Consultar a API FIPE no momento do cadastro e da atualização de veículos para verificar se a marca informada existe na tabela oficial.

## Fluxo

```
Cliente → POST /veiculos
       → CadastrarVeiculoService.validarMarcaFipe(marca)
       → FipePort.listarMarcasCarros()      ← cache 1h
       → FipeClientAdapter → GET /cars/brands (API FIPE)
       → Marca encontrada? → prosseguir
       → Marca não encontrada? → MarcaInvalidaFipeException (422)
```

## Considerações

- A validação é **síncrona e bloqueante** — a operação não prossegue sem confirmar a marca.
- O cache de 1 hora evita chamadas repetidas para cada cadastro — marcas raramente mudam.
- Apenas o tipo `cars` é validado nesta versão; motos e caminhões podem ser incluídos em iteração futura via parâmetro de tipo.
- A comparação de nome é case-insensitive para tolerar variações de digitação.
