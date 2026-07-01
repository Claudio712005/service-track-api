package br.com.servicetrack.application.notificacao.ports.`in`

import br.com.servicetrack.application.notificacao.dto.NotificacaoResDTO
import br.com.servicetrack.application.shared.dto.PageResDTO

interface ListarNotificacoesUseCase {

    fun executar(visualizada: Boolean?, page: Int, size: Int): PageResDTO<NotificacaoResDTO>
}
