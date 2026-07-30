package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.notificacao.dto.ContadorNaoLidasResDTO
import br.com.servicetrack.application.notificacao.ports.`in`.ContarNotificacoesNaoLidasUseCase
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.observabilidade.annotation.Observavel
import br.com.servicetrack.application.observabilidade.enums.CodigoUseCase
import br.com.servicetrack.application.usuario.ports.out.JwtPort

class ContarNotificacoesNaoLidasUseCaseImpl(
    private val repository: NotificacaoRepositoryPort,
    private val jwt: JwtPort,
) : ContarNotificacoesNaoLidasUseCase {

    @Observavel(codigo = CodigoUseCase.NOTIFICACAO_CONTAR_NAO_LIDAS)

    override fun executar(): ContadorNaoLidasResDTO {
        val usuarioId = jwt.getUsuarioId()
        val total = repository.contarNaoLidas(usuarioId)
        return ContadorNaoLidasResDTO(total)
    }
}
