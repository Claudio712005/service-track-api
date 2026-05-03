package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO

interface FinalizarOrdemServicoUseCase {
    fun finalizarOrdemServico(ordemServicoId: String): ResumoOrdemServicoResDTO
}
