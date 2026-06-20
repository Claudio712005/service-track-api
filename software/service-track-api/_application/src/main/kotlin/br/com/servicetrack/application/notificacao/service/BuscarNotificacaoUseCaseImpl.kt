package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.notificacao.dto.NotificacaoResDTO
import br.com.servicetrack.application.notificacao.ports.`in`.BuscarNotificacaoUseCase
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import java.util.UUID

class BuscarNotificacaoUseCaseImpl(
    private val repository: NotificacaoRepositoryPort,
    private val jwt: JwtPort,
) : BuscarNotificacaoUseCase {

    override fun executar(id: String): NotificacaoResDTO {
        val usuarioId = jwt.getUsuarioId()

        val notificacao = repository.buscarPorId(NotificacaoId.de(UUID.fromString(id)))
            ?: throw EntidadeNaoEncontradaException("Notificação", arrayOf(id))

        if (notificacao.destinatario != usuarioId) {
            throw OperacaoNegadaException("buscarNotificacao", "notificação não pertence ao usuário")
        }

        return NotificacaoResDTO.de(notificacao)
    }
}
