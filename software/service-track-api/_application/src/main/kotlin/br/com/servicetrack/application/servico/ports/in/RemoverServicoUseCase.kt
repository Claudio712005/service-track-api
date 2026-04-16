package br.com.servicetrack.application.servico.ports.`in`

import br.com.servicetrack.domain.servico.vo.ServicoId

interface RemoverServicoUseCase {
    fun removerServico(id: ServicoId)
}
