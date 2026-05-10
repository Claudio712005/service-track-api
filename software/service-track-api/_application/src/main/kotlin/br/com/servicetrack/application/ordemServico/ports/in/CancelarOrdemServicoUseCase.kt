package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.request.CancelarOsReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO

interface CancelarOrdemServicoUseCase {
    fun cancelarOrdemServico(ordemServicoId: String, req: CancelarOsReqDTO): ResumoOrdemServicoResDTO
}
