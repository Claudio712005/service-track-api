package br.com.servicetrack.application.notificacao.ports.`in`

import br.com.servicetrack.application.notificacao.dto.NotificacaoResDTO

interface BuscarNotificacaoUseCase {

    fun executar(id: String): NotificacaoResDTO
}
