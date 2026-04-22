package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO

interface BuscarOrdemServicoUseCase {
    fun buscarOrdemServico(ordemServicoId: String): OrdemServicoResDTO
}
