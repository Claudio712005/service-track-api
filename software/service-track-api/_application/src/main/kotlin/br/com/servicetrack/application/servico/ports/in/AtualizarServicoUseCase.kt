package br.com.servicetrack.application.servico.ports.`in`

import br.com.servicetrack.application.servico.dto.AtualizarServicoReqDTO
import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.domain.servico.vo.ServicoId

interface AtualizarServicoUseCase {
    fun atualizarServico(id: ServicoId, req: AtualizarServicoReqDTO): ServicoResDTO
}
