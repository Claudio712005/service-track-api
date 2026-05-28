package br.com.servicetrack.application.notificacao.ports.out

import br.com.servicetrack.domain.notificacao.Notificacao
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId

interface NotificacaoRepositoryPort {

    fun salvar(notificacao: Notificacao)

    fun atualizar(notificacao: Notificacao)

    fun buscarPorId(id: NotificacaoId): Notificacao?

    fun buscarPendentesParaEnvio(limite: Int): List<Notificacao>
}

