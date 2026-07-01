package br.com.servicetrack.application.notificacao.ports.`in`

import br.com.servicetrack.application.notificacao.dto.EnfileirarNotificacaoCommand
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId

interface EnfileirarNotificacaoUseCase {

    fun executar(comando: EnfileirarNotificacaoCommand): NotificacaoId
}

