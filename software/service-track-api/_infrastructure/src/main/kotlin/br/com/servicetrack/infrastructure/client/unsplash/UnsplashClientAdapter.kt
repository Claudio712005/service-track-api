package br.com.servicetrack.infrastructure.client.unsplash

import br.com.servicetrack.application.veiculo.ports.out.UnsplashPort
import io.quarkus.cache.CacheResult
import io.smallrye.faulttolerance.api.ExponentialBackoff
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.faulttolerance.CircuitBreaker
import org.eclipse.microprofile.faulttolerance.Fallback
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger

@ApplicationScoped
class UnsplashClientAdapter(
    @RestClient private val unsplashClient: UnsplashClient,
    @ConfigProperty(name = "unsplash.chave-acesso", defaultValue = "") private val chaveAcesso: String,
) : UnsplashPort {

    private val log = Logger.getLogger(UnsplashClientAdapter::class.java)

    @CacheResult(cacheName = "unsplash-imagens-veiculo")
    @Retry(maxRetries = 3)
    @ExponentialBackoff(factor = 2, maxDelay = 10000)
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 1.0, delay = 60000, successThreshold = 2)
    @Timeout(10000)
    @Fallback(fallbackMethod = "buscarImagensFallback")
    override fun buscarImagensVeiculo(marca: String, modelo: String, quantidade: Int): List<String> {
        if (chaveAcesso.isBlank()) {
            log.warn("[Unsplash] Chave de acesso não configurada. Retornando lista vazia.")
            return emptyList()
        }

        val consulta = "$marca $modelo carro"
        val resposta = unsplashClient.buscarFotos(
            consulta = consulta,
            quantidade = quantidade,
            autorizacao = "Client-ID $chaveAcesso",
        )
        return resposta.resultados.map { it.urls.regular }
    }

    @Suppress("unused")
    private fun buscarImagensFallback(marca: String, modelo: String, quantidade: Int): List<String> {
        log.warnf("[Unsplash] Fallback ativado para buscarImagensVeiculo('%s', '%s') — retornando lista vazia", marca, modelo)
        return emptyList()
    }
}
