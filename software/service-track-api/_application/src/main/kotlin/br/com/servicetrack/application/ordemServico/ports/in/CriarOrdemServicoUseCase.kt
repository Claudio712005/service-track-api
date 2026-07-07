package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.orcamento.dto.res.OrcamentoResDTO
import br.com.servicetrack.application.ordemServico.dto.request.OrdemServicoReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO

interface CriarOrdemServicoUseCase {

    fun criarOrdemServico(req: OrdemServicoReqDTO): ResumoOrdemServicoResDTO
}
