package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.request.FiltroOrdemServicoDTO
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.shared.dto.PageResDTO

interface ListarOrdensServicoUseCase {
    fun listarOrdensServico(filtro: FiltroOrdemServicoDTO): PageResDTO<ResumoOrdemServicoResDTO>
}
