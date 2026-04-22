package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.request.AssociarItensReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO

interface AssociarItensOrdemServicoUseCase {
    fun associarItens(ordemServicoId: String, req: AssociarItensReqDTO): OrdemServicoResDTO
}
