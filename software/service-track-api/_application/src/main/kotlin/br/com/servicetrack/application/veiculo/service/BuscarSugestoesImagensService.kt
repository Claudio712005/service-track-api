package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.veiculo.dto.response.SugestoesImagensResDTO
import br.com.servicetrack.application.veiculo.ports.`in`.BuscarSugestoesImagensUseCase
import br.com.servicetrack.application.veiculo.ports.out.UnsplashPort

private const val QUANTIDADE_MAXIMA_SUGESTOES = 10

class BuscarSugestoesImagensService(
    private val unsplash: UnsplashPort
) : BuscarSugestoesImagensUseCase {

    override fun buscarSugestoes(marca: String, modelo: String): SugestoesImagensResDTO {
        val imagens = unsplash.buscarImagensVeiculo(
            marca = marca.trim(),
            modelo = modelo.trim(),
            quantidade = QUANTIDADE_MAXIMA_SUGESTOES
        )
        return SugestoesImagensResDTO(imagens = imagens)
    }
}
