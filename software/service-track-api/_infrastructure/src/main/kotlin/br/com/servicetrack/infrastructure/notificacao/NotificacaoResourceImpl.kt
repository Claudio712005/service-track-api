package br.com.servicetrack.infrastructure.notificacao

import br.com.servicetrack.application.notificacao.ports.`in`.BuscarNotificacaoUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.ContarNotificacoesNaoLidasUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.ListarNotificacoesUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.MarcarNotificacaoVisualizadaUseCase
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.infrastructure.api.NotificacoesApi
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import java.util.UUID

@ApplicationScoped
class NotificacaoResourceImpl @Inject constructor(
    private val listarNotificacoesUseCase: ListarNotificacoesUseCase,
    private val buscarNotificacaoUseCase: BuscarNotificacaoUseCase,
    private val contarNotificacoesNaoLidasUseCase: ContarNotificacoesNaoLidasUseCase,
    private val marcarNotificacaoVisualizadaUseCase: MarcarNotificacaoVisualizadaUseCase,
) : NotificacoesApi {

    @RolesAllowed("CLIENTE", "MECANICO")
    override fun listarNotificacoes(visualizada: Boolean?, page: Int?, size: Int?): Response {
        val resultado = listarNotificacoesUseCase.executar(visualizada, page ?: 0, size ?: 20)
        return Response.ok(resultado).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    override fun buscarNotificacao(id: UUID): Response {
        val resultado = buscarNotificacaoUseCase.executar(id.toString())
        return Response.ok(resultado).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    override fun contarNotificacoesNaoLidas(): Response {
        val resultado = contarNotificacoesNaoLidasUseCase.executar()
        return Response.ok(resultado).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    @Transactional
    override fun marcarNotificacaoVisualizada(id: UUID): Response {
        marcarNotificacaoVisualizadaUseCase.executar(NotificacaoId.de(id))
        return Response.noContent().build()
    }
}
