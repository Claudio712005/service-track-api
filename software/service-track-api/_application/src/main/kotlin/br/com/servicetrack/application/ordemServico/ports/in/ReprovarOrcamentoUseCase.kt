package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.request.ReprovarOrcamentoReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO

interface ReprovarOrcamentoUseCase {
    fun reprovarOrcamento(ordemServicoId: String, req: ReprovarOrcamentoReqDTO): ResumoOrdemServicoResDTO
}
