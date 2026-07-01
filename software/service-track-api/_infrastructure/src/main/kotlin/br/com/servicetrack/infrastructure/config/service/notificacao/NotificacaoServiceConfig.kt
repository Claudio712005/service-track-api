package br.com.servicetrack.infrastructure.config.service.notificacao

import br.com.servicetrack.application.notificacao.ports.`in`.BuscarNotificacaoUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.ContarNotificacoesNaoLidasUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.ListarNotificacoesUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.MarcarNotificacaoVisualizadaUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.ProcessarNotificacoesPendentesUseCase
import br.com.servicetrack.application.notificacao.ports.out.EmailDestinatarioResolverPort
import br.com.servicetrack.application.notificacao.ports.out.EmailGatewayPort
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.notificacao.ports.out.TemplateRendererPort
import br.com.servicetrack.application.notificacao.service.BuscarNotificacaoUseCaseImpl
import br.com.servicetrack.application.notificacao.service.ContarNotificacoesNaoLidasUseCaseImpl
import br.com.servicetrack.application.notificacao.service.EnfileirarNotificacaoUseCaseImpl
import br.com.servicetrack.application.notificacao.service.ListarNotificacoesUseCaseImpl
import br.com.servicetrack.application.notificacao.service.MarcarNotificacaoVisualizadaUseCaseImpl
import br.com.servicetrack.application.notificacao.service.ProcessarNotificacoesPendentesUseCaseImpl
import br.com.servicetrack.application.shared.ports.out.TransactionRunnerPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class NotificacaoServiceConfig {

    @Produces
    @ApplicationScoped
    fun enfileirarNotificacaoUseCase(
        repository: NotificacaoRepositoryPort,
    ): EnfileirarNotificacaoUseCase = EnfileirarNotificacaoUseCaseImpl(repository)

    @Produces
    @ApplicationScoped
    fun marcarNotificacaoVisualizadaUseCase(
        repository: NotificacaoRepositoryPort,
        jwt: JwtPort,
    ): MarcarNotificacaoVisualizadaUseCase = MarcarNotificacaoVisualizadaUseCaseImpl(repository, jwt)

    @Produces
    @ApplicationScoped
    fun listarNotificacoesUseCase(
        repository: NotificacaoRepositoryPort,
        jwt: JwtPort,
    ): ListarNotificacoesUseCase = ListarNotificacoesUseCaseImpl(repository, jwt)

    @Produces
    @ApplicationScoped
    fun buscarNotificacaoUseCase(
        repository: NotificacaoRepositoryPort,
        jwt: JwtPort,
    ): BuscarNotificacaoUseCase = BuscarNotificacaoUseCaseImpl(repository, jwt)

    @Produces
    @ApplicationScoped
    fun contarNotificacoesNaoLidasUseCase(
        repository: NotificacaoRepositoryPort,
        jwt: JwtPort,
    ): ContarNotificacoesNaoLidasUseCase = ContarNotificacoesNaoLidasUseCaseImpl(repository, jwt)

    @Produces
    @ApplicationScoped
    fun processarNotificacoesPendentesUseCase(
        repository: NotificacaoRepositoryPort,
        renderer: TemplateRendererPort,
        emailGateway: EmailGatewayPort,
        destinatarioResolver: EmailDestinatarioResolverPort,
        transactionRunner: TransactionRunnerPort,
    ): ProcessarNotificacoesPendentesUseCase = ProcessarNotificacoesPendentesUseCaseImpl(
        repository = repository,
        renderer = renderer,
        emailGateway = emailGateway,
        destinatarioResolver = destinatarioResolver,
        transactionRunner = transactionRunner,
    )
}
