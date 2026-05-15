package br.com.servicetrack.infrastructure.config.service.dashboard

import br.com.servicetrack.application.dashboard.ports.`in`.BuscarResumoDashClienteUseCase
import br.com.servicetrack.application.dashboard.service.BuscarResumoDashClienteService
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class DashboardServiceConfig {

    @Produces
    @ApplicationScoped
    fun buscarResumoDashClienteUseCase(
        usuarioRepositoryPort: UsuarioRepositoryPort,
        veiculoRepositoryPort: VeiculoRepositoryPort,
        ordemServicoRepositoryPort: OrdemServicoRepositoryPort,
        jwtPort: JwtPort,
    ): BuscarResumoDashClienteUseCase = BuscarResumoDashClienteService(
        usuarioRepositoryPort,
        veiculoRepositoryPort,
        ordemServicoRepositoryPort,
        jwtPort,
    )
}
