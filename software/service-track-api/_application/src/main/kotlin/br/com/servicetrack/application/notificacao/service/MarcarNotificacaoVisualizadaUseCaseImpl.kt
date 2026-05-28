package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.annotation.ApplicationService
import br.com.servicetrack.application.notificacao.ports.`in`.MarcarNotificacaoVisualizadaUseCase
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.shared.exception.DomainException

@ApplicationService
class MarcarNotificacaoVisualizadaUseCaseImpl(
    private val repository: NotificacaoRepositoryPort,
) : MarcarNotificacaoVisualizadaUseCase {

    override fun executar(id: NotificacaoId) {
        val notificacao = repository.buscarPorId(id)
            ?: throw DomainException("Notificação ${id.value} não encontrada")
        notificacao.visualizar()
        repository.atualizar(notificacao)
    }
}

