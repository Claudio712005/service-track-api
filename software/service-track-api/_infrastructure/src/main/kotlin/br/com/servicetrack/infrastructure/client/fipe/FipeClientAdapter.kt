package br.com.servicetrack.infrastructure.client.fipe

import br.com.servicetrack.application.exception.IntegracaoExternaException
import br.com.servicetrack.application.veiculo.dto.fipe.AnoFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.DetalheVeiculoFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.MarcaFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.ModeloFipeDTO
import br.com.servicetrack.application.veiculo.ports.out.FipePort
import io.quarkus.cache.CacheResult
import io.smallrye.faulttolerance.api.ExponentialBackoff
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.faulttolerance.CircuitBreaker
import org.eclipse.microprofile.faulttolerance.Fallback
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger

@ApplicationScoped
class FipeClientAdapter(
    @RestClient private val fipeClient: FipeClient,
) : FipePort {

    private val log = Logger.getLogger(FipeClientAdapter::class.java)

    @CacheResult(cacheName = "fipe-marcas-carros")
    @Retry(maxRetries = 3)
    @ExponentialBackoff(factor = 2, maxDelay = 10000)
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 1.0, delay = 60000, successThreshold = 2)
    @Timeout(10000)
    @Fallback(fallbackMethod = "listarMarcasFallback")
    override fun listarMarcasCarros(): List<MarcaFipeDTO> {
        return try {
            fipeClient.listarMarcasCarros().map { MarcaFipeDTO(codigo = it.codigo, nome = it.nome) }
        } catch (ex: Exception) {
            log.errorf(ex, "[FIPE] Falha ao consultar marcas — tentativa será retentada se houver retries restantes")
            throw IntegracaoExternaException("FIPE", "Não foi possível consultar as marcas disponíveis")
        }
    }

    @CacheResult(cacheName = "fipe-modelos-carros")
    @Retry(maxRetries = 3)
    @ExponentialBackoff(factor = 2, maxDelay = 10000)
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 1.0, delay = 60000, successThreshold = 2)
    @Timeout(10000)
    @Fallback(fallbackMethod = "listarModelosFallback")
    override fun listarModelosCarros(codigoMarca: String): List<ModeloFipeDTO> {
        return try {
            fipeClient.listarModelosCarros(codigoMarca).map { ModeloFipeDTO(codigo = it.codigo, nome = it.nome) }
        } catch (ex: Exception) {
            log.errorf(ex, "[FIPE] Falha ao consultar modelos da marca %s", codigoMarca)
            throw IntegracaoExternaException("FIPE", "Não foi possível consultar os modelos da marca informada")
        }
    }

    @CacheResult(cacheName = "fipe-anos-carros")
    @Retry(maxRetries = 3)
    @ExponentialBackoff(factor = 2, maxDelay = 10000)
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 1.0, delay = 60000, successThreshold = 2)
    @Timeout(10000)
    @Fallback(fallbackMethod = "listarAnosFallback")
    override fun listarAnosCarros(codigoMarca: String, codigoModelo: String): List<AnoFipeDTO> {
        return try {
            fipeClient.listarAnosCarros(codigoMarca, codigoModelo)
                .map { AnoFipeDTO(codigo = it.codigo, nome = it.nome) }
        } catch (ex: Exception) {
            log.errorf(ex, "[FIPE] Falha ao consultar anos do modelo %s/%s", codigoMarca, codigoModelo)
            throw IntegracaoExternaException("FIPE", "Não foi possível consultar os anos do modelo informado")
        }
    }

    @Retry(maxRetries = 3)
    @ExponentialBackoff(factor = 2, maxDelay = 10000)
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 1.0, delay = 60000, successThreshold = 2)
    @Timeout(10000)
    @Fallback(fallbackMethod = "consultarDetalhesFallback")
    override fun consultarDetalhesCarros(codigoMarca: String, codigoModelo: String, ano: String): DetalheVeiculoFipeDTO {
        return try {
            val resposta = fipeClient.consultarDetalhesCarros(codigoMarca, codigoModelo, ano)
            DetalheVeiculoFipeDTO(
                codigoFipe = resposta.codigoFipe,
                marca = resposta.marca,
                modelo = resposta.modelo,
                anoModelo = resposta.anoModelo,
                combustivel = resposta.combustivel,
                valor = resposta.valor,
                mesReferencia = resposta.mesReferencia,
            )
        } catch (ex: Exception) {
            log.errorf(ex, "[FIPE] Falha ao consultar detalhes %s/%s/%s", codigoMarca, codigoModelo, ano)
            throw IntegracaoExternaException("FIPE", "Não foi possível consultar os detalhes do veículo informado")
        }
    }

    @Suppress("unused")
    private fun listarMarcasFallback(): List<MarcaFipeDTO> {
        log.errorf("[FIPE] Fallback ativado para listarMarcasCarros — circuito aberto ou retries esgotados")
        throw IntegracaoExternaException("FIPE", "Serviço FIPE temporariamente indisponível após falhas consecutivas")
    }

    @Suppress("unused")
    private fun listarModelosFallback(codigoMarca: String): List<ModeloFipeDTO> {
        log.errorf("[FIPE] Fallback ativado para listarModelosCarros(%s) — circuito aberto ou retries esgotados", codigoMarca)
        throw IntegracaoExternaException("FIPE", "Serviço FIPE temporariamente indisponível após falhas consecutivas")
    }

    @Suppress("unused")
    private fun listarAnosFallback(codigoMarca: String, codigoModelo: String): List<AnoFipeDTO> {
        log.errorf("[FIPE] Fallback ativado para listarAnosCarros(%s/%s) — circuito aberto ou retries esgotados", codigoMarca, codigoModelo)
        throw IntegracaoExternaException("FIPE", "Serviço FIPE temporariamente indisponível após falhas consecutivas")
    }

    @Suppress("unused")
    private fun consultarDetalhesFallback(codigoMarca: String, codigoModelo: String, ano: String): DetalheVeiculoFipeDTO {
        log.errorf("[FIPE] Fallback ativado para consultarDetalhesCarros(%s/%s/%s) — circuito aberto ou retries esgotados", codigoMarca, codigoModelo, ano)
        throw IntegracaoExternaException("FIPE", "Serviço FIPE temporariamente indisponível após falhas consecutivas")
    }
}
