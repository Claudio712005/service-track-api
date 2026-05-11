# ADR-008 — Escolha do Cliente HTTP: MicroProfile REST Client

**Status:** Aceito  
**Data:** 2026-05-10  
**Autores:** Equipe ServiceTrack

---

## Contexto

O projeto precisou escolher uma biblioteca para consumo de APIs HTTP externas (FIPE e Unsplash). As opções avaliadas foram:

- **Retrofit 2** (Square) — cliente HTTP declarativo popular no ecossistema Android/JVM
- **Feign** (OpenFeign/Netflix) — cliente declarativo amplamente usado com Spring
- **MicroProfile REST Client** — especificação Jakarta/MicroProfile, nativa ao Quarkus

---

## Decisão

Adotar o **MicroProfile REST Client** (`quarkus-rest-client-reactive-jackson`).

### Justificativa

| Critério | MicroProfile REST Client | Retrofit | Feign |
|---|---|---|---|
| Integração nativa com Quarkus | ✅ Nativa | ❌ Requer configuração manual | ⚠️ Suportado via extension |
| CDI / Injeção de dependência | ✅ `@RestClient` nativo | ❌ Manual | ⚠️ Parcial |
| Estilo declarativo (interfaces) | ✅ | ✅ | ✅ |
| Configuração via `application.properties` | ✅ | ❌ | ⚠️ |
| Reactive (Vert.x) compatível | ✅ Reactive nativo | ❌ | ⚠️ |
| Dependências extras | Nenhuma (já no BOM) | 3+ deps | 2+ deps |
| Observabilidade / Tracing | ✅ Integrado ao OpenTelemetry | ❌ Manual | ⚠️ |

O MicroProfile REST Client é a escolha canônica para projetos Quarkus. Não adiciona dependências externas (já faz parte do Quarkus BOM), integra-se nativamente ao CDI para injeção com `@RestClient`, e a configuração das URLs e timeouts é feita diretamente no `application.properties` sem código extra.

### Padrão adotado

```kotlin
@RegisterRestClient(configKey = "fipe-api")
@Path("/")
interface FipeClient {
    @GET
    @Path("/cars/brands")
    fun listarMarcasCarros(): List<FipeMarcaResponse>
}
```

```properties
quarkus.rest-client.fipe-api.url=https://fipe.parallelum.com.br/api/v2
quarkus.rest-client.fipe-api.connect-timeout=5000
quarkus.rest-client.fipe-api.read-timeout=10000
```

---

## Consequências

**Positivas:**
- Zero dependências novas além de `quarkus-rest-client-reactive-jackson` (já no ecossistema).
- Configuração totalmente externalizada via properties — fácil de mudar por ambiente.
- Injeção direta via `@RestClient` no adapter sem boilerplate.

**Negativas:**
- Curva de aprendizado para desenvolvedores vindos de Spring + Feign.
- Menos exemplos no mercado comparado ao Retrofit + Kotlin Coroutines.

---

## Alternativas rejeitadas

**Retrofit:**  
Popular mas orientado a Android. Requer configuração manual de OkHttpClient, serialização e integração com CDI. Gera overhead desnecessário em projetos Quarkus.

**Feign (OpenFeign):**  
Mais próximo do ambiente Quarkus via extension, mas requer dependência extra e tem integração CDI menos madura que o MicroProfile REST Client nativo. Adiciona complexidade sem benefício concreto.
