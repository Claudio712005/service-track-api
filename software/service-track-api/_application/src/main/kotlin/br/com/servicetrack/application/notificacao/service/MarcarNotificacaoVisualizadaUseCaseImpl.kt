package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.annotation.ApplicationService
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.notificacao.ports.`in`.MarcarNotificacaoVisualizadaUseCase
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId

@ApplicationService
class MarcarNotificacaoVisualizadaUseCaseImpl(
    private val repository: NotificacaoRepositoryPort,
    private val jwt: JwtPort,
) : MarcarNotificacaoVisualizadaUseCase {

    override fun executar(id: NotificacaoId) {
        val usuarioId = jwt.getUsuarioId()

        val notificacao = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException("Notificação", arrayOf(id.value))

        if (notificacao.destinatario != usuarioId) {
            throw OperacaoNegadaException("visualizarNotificacao", "notificação não pertence ao usuário")
        }

        notificacao.visualizar()
        repository.atualizar(notificacao)
    }
}

