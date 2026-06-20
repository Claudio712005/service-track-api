package br.com.servicetrack.application.notificacao.ports.out

import br.com.servicetrack.application.notificacao.dto.FiltroNotificacaoDTO
import br.com.servicetrack.application.shared.dto.PageResDTO
import br.com.servicetrack.domain.notificacao.Notificacao
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface NotificacaoRepositoryPort {

    fun salvar(notificacao: Notificacao)

    fun atualizar(notificacao: Notificacao)

    fun buscarPorId(id: NotificacaoId): Notificacao?

    fun buscarPendentesParaEnvio(limite: Int): List<Notificacao>

    fun listar(filtro: FiltroNotificacaoDTO): PageResDTO<Notificacao>

    fun contarNaoLidas(destinatarioId: UsuarioId): Long
}

