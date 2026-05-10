package br.com.servicetrack.application.servico.ports.`in`

import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.domain.servico.vo.ServicoId

interface BuscarServicoUseCase {
    fun buscarServico(id: ServicoId): ServicoResDTO
}
