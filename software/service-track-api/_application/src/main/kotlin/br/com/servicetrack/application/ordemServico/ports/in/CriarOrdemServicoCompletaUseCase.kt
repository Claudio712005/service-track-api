package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.request.CriarOrdemServicoCompletaReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO

interface CriarOrdemServicoCompletaUseCase {

    fun criarOrdemServicoCompleta(req: CriarOrdemServicoCompletaReqDTO): OrdemServicoResDTO
}
