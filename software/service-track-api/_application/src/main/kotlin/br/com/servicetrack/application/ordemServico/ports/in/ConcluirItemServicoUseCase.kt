package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.request.ConcluirItemServicoReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO

interface ConcluirItemServicoUseCase {
    fun concluirItemServico(ordemServicoId: String, itemServicoId: String, req: ConcluirItemServicoReqDTO): OrdemServicoResDTO
}
