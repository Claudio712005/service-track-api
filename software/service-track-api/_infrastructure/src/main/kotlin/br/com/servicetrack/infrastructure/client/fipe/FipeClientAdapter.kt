package br.com.servicetrack.infrastructure.client.fipe

import br.com.servicetrack.application.exception.IntegracaoExternaException
import br.com.servicetrack.application.veiculo.dto.fipe.AnoFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.DetalheVeiculoFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.MarcaFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.ModeloFipeDTO
import br.com.servicetrack.application.veiculo.ports.out.FipePort
import io.quarkus.cache.CacheResult
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.logging.Logger

@ApplicationScoped
class FipeClientAdapter(
    @RestClient private val fipeClient: FipeClient
) : FipePort {

    private val log = Logger.getLogger(FipeClientAdapter::class.java)

    @CacheResult(cacheName = "fipe-marcas-carros")
    override fun listarMarcasCarros(): List<MarcaFipeDTO> {
        return try {
            fipeClient.listarMarcasCarros().map { MarcaFipeDTO(codigo = it.codigo, nome = it.nome) }
        } catch (ex: Exception) {
            log.errorf(ex, "Falha ao consultar marcas na API FIPE")
            throw IntegracaoExternaException("FIPE", "Não foi possível consultar as marcas disponíveis")
        }
    }

    @CacheResult(cacheName = "fipe-modelos-carros")
    override fun listarModelosCarros(codigoMarca: String): List<ModeloFipeDTO> {
        return try {
            fipeClient.listarModelosCarros(codigoMarca).map { ModeloFipeDTO(codigo = it.codigo, nome = it.nome) }
        } catch (ex: Exception) {
            log.errorf(ex, "Falha ao consultar modelos da marca %s na API FIPE", codigoMarca)
            throw IntegracaoExternaException("FIPE", "Não foi possível consultar os modelos da marca informada")
        }
    }

    @CacheResult(cacheName = "fipe-anos-carros")
    override fun listarAnosCarros(codigoMarca: String, codigoModelo: String): List<AnoFipeDTO> {
        return try {
            fipeClient.listarAnosCarros(codigoMarca, codigoModelo)
                .map { AnoFipeDTO(codigo = it.codigo, nome = it.nome) }
        } catch (ex: Exception) {
            log.errorf(ex, "Falha ao consultar anos do modelo %s/%s na API FIPE", codigoMarca, codigoModelo)
            throw IntegracaoExternaException("FIPE", "Não foi possível consultar os anos do modelo informado")
        }
    }

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
                mesReferencia = resposta.mesReferencia
            )
        } catch (ex: Exception) {
            log.errorf(ex, "Falha ao consultar detalhes %s/%s/%s na API FIPE", codigoMarca, codigoModelo, ano)
            throw IntegracaoExternaException("FIPE", "Não foi possível consultar os detalhes do veículo informado")
        }
    }
}
