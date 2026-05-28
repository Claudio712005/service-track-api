package br.com.servicetrack.infrastructure.config.service.notificacao

import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.MarcarNotificacaoVisualizadaUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.ProcessarNotificacoesPendentesUseCase
import br.com.servicetrack.application.notificacao.ports.out.EmailDestinatarioResolverPort
import br.com.servicetrack.application.notificacao.ports.out.EmailGatewayPort
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.notificacao.ports.out.TemplateRendererPort
import br.com.servicetrack.application.notificacao.service.EnfileirarNotificacaoUseCaseImpl
import br.com.servicetrack.application.notificacao.service.MarcarNotificacaoVisualizadaUseCaseImpl
import br.com.servicetrack.application.notificacao.service.ProcessarNotificacoesPendentesUseCaseImpl
import br.com.servicetrack.application.shared.ports.out.TransactionRunnerPort
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
    ): MarcarNotificacaoVisualizadaUseCase = MarcarNotificacaoVisualizadaUseCaseImpl(repository)

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
