package br.com.servicetrack.infrastructure.config.service

import br.com.servicetrack.application.auditoria.ports.out.AuditoriaRepositoryPort
import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.infrastructure.auditoria.RegistrarAuditoriaAdapter
import br.com.servicetrack.infrastructure.auditoria.factory.AuditoriaStrategyFactory
import br.com.servicetrack.infrastructure.auditoria.strategy.AtualizacaoAuditoriaStrategy
import br.com.servicetrack.infrastructure.auditoria.strategy.CriacaoAuditoriaStrategy
import br.com.servicetrack.infrastructure.auditoria.strategy.LoginAuditoriaStrategy
import br.com.servicetrack.infrastructure.auditoria.strategy.LogoutAuditoriaStrategy
import br.com.servicetrack.infrastructure.auditoria.strategy.RemocaoAuditoriaStrategy
import io.vertx.ext.web.RoutingContext
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class ApplicationServiceConfig {

    @Produces
    @ApplicationScoped
    fun auditoriaStrategyFactory(): AuditoriaStrategyFactory =
        AuditoriaStrategyFactory(
            listOf(
                CriacaoAuditoriaStrategy(),
                AtualizacaoAuditoriaStrategy(),
                RemocaoAuditoriaStrategy(),
                LoginAuditoriaStrategy(),
                LogoutAuditoriaStrategy(),
            )
        )

    @Produces
    @ApplicationScoped
    fun registrarAuditoriaPort(
        jwtPort: JwtPort,
        strategyFactory: AuditoriaStrategyFactory,
        repository: AuditoriaRepositoryPort,
        routingContext: RoutingContext,
    ): RegistrarAuditoriaPort = RegistrarAuditoriaAdapter(jwtPort, strategyFactory, repository, routingContext)

}