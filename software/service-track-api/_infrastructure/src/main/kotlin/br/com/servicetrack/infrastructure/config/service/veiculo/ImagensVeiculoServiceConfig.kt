package br.com.servicetrack.infrastructure.config.service.veiculo

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.veiculo.ports.`in`.BuscarSugestoesImagensUseCase
import br.com.servicetrack.application.veiculo.ports.out.UnsplashPort
import br.com.servicetrack.application.veiculo.service.BuscarSugestoesImagensService
import br.com.servicetrack.infrastructure.observabilidade.UseCaseProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class ImagensVeiculoServiceConfig(
    private val proxy: UseCaseProxy,
    private val auditoria: RegistrarAuditoriaPort,
) {

    @Produces
    @ApplicationScoped
    fun buscarSugestoesImagensUseCase(
        unsplash: UnsplashPort
    ): BuscarSugestoesImagensUseCase = proxy.envolver(
        BuscarSugestoesImagensService(unsplash),
        BuscarSugestoesImagensUseCase::class.java,
        auditoria,
    )
}
