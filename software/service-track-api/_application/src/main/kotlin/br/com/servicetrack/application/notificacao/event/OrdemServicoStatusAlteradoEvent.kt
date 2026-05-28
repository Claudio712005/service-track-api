package br.com.servicetrack.application.notificacao.event

import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId

data class OrdemServicoStatusAlteradoEvent(
    val ordemServicoId: OrdemServicoId,
    val clienteId: UsuarioId,
    val novoStatus: StatusOrdemServicoEnum,
)
