package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.notificacao.dto.FiltroNotificacaoDTO
import br.com.servicetrack.application.notificacao.dto.NotificacaoResDTO
import br.com.servicetrack.application.notificacao.ports.`in`.ListarNotificacoesUseCase
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.shared.dto.PageResDTO
import br.com.servicetrack.application.usuario.ports.out.JwtPort

class ListarNotificacoesUseCaseImpl(
    private val repository: NotificacaoRepositoryPort,
    private val jwt: JwtPort,
) : ListarNotificacoesUseCase {

    override fun executar(visualizada: Boolean?, page: Int, size: Int): PageResDTO<NotificacaoResDTO> {
        val usuarioId = jwt.getUsuarioId()

        val filtro = FiltroNotificacaoDTO(
            destinatarioId = usuarioId.valor,
            visualizada = visualizada,
            page = page,
            size = size,
        )

        val pagina = repository.listar(filtro)
        return PageResDTO.de(
            content = pagina.content.map { NotificacaoResDTO.de(it) },
            page = pagina.page,
            size = pagina.size,
            total = pagina.total,
        )
    }
}
