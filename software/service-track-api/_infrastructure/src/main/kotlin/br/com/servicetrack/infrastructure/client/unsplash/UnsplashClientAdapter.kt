package br.com.servicetrack.infrastructure.client.unsplash

import br.com.servicetrack.application.veiculo.ports.out.UnsplashPort
import io.quarkus.cache.CacheResult
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger

@ApplicationScoped
class UnsplashClientAdapter(
    @RestClient private val unsplashClient: UnsplashClient,
    @ConfigProperty(name = "unsplash.chave-acesso", defaultValue = "") private val chaveAcesso: String
) : UnsplashPort {

    private val log = Logger.getLogger(UnsplashClientAdapter::class.java)

    @CacheResult(cacheName = "unsplash-imagens-veiculo")
    override fun buscarImagensVeiculo(marca: String, modelo: String, quantidade: Int): List<String> {
        if (chaveAcesso.isBlank()) {
            log.warn("Chave de acesso Unsplash não configurada. Retornando lista vazia.")
            return emptyList()
        }

        return try {
            val consulta = "$marca $modelo carro"
            val resposta = unsplashClient.buscarFotos(
                consulta = consulta,
                quantidade = quantidade,
                autorizacao = "Client-ID $chaveAcesso"
            )
            resposta.resultados.map { it.urls.regular }
        } catch (ex: Exception) {
            log.warnf(ex, "Falha ao buscar imagens no Unsplash para '%s %s'. Retornando lista vazia.", marca, modelo)
            emptyList()
        }
    }
}
