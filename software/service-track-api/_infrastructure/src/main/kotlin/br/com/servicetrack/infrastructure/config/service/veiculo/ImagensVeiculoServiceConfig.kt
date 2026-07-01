package br.com.servicetrack.infrastructure.config.service.veiculo

import br.com.servicetrack.application.veiculo.ports.`in`.BuscarSugestoesImagensUseCase
import br.com.servicetrack.application.veiculo.ports.out.UnsplashPort
import br.com.servicetrack.application.veiculo.service.BuscarSugestoesImagensService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class ImagensVeiculoServiceConfig {

    @Produces
    @ApplicationScoped
    fun buscarSugestoesImagensUseCase(
        unsplash: UnsplashPort
    ): BuscarSugestoesImagensUseCase = BuscarSugestoesImagensService(unsplash)
}
