package br.com.servicetrack.application.notificacao.ports.`in`

import br.com.servicetrack.domain.notificacao.vo.NotificacaoId

interface MarcarNotificacaoVisualizadaUseCase {

    fun executar(id: NotificacaoId)
}

