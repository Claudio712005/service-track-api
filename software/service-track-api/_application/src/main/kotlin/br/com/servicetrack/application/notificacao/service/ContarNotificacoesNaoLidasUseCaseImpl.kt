package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.notificacao.dto.ContadorNaoLidasResDTO
import br.com.servicetrack.application.notificacao.ports.`in`.ContarNotificacoesNaoLidasUseCase
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort

class ContarNotificacoesNaoLidasUseCaseImpl(
    private val repository: NotificacaoRepositoryPort,
    private val jwt: JwtPort,
) : ContarNotificacoesNaoLidasUseCase {

    override fun executar(): ContadorNaoLidasResDTO {
        val usuarioId = jwt.getUsuarioId()
        val total = repository.contarNaoLidas(usuarioId)
        return ContadorNaoLidasResDTO(total)
    }
}
