package br.com.servicetrack.application.servico.ports.`in`

import br.com.servicetrack.application.servico.dto.CriarServicoReqDTO
import br.com.servicetrack.application.servico.dto.ServicoResDTO

interface CriarServicoUseCase {
    fun criarServico(req: CriarServicoReqDTO): ServicoResDTO
}
