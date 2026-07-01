package br.com.servicetrack.application.veiculo.ports.`in`

import br.com.servicetrack.application.veiculo.dto.response.SugestoesImagensResDTO

interface BuscarSugestoesImagensUseCase {
    fun buscarSugestoes(marca: String, modelo: String): SugestoesImagensResDTO
}
