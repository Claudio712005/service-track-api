package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO

interface EntregarOrdemServicoUseCase {
    fun entregarOrdemServico(ordemServicoId: String): ResumoOrdemServicoResDTO
}
