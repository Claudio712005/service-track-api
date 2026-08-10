package br.com.servicetrack.infrastructure.config.service.dashboard

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.dashboard.ports.`in`.BuscarResumoDashClienteUseCase
import br.com.servicetrack.application.dashboard.service.BuscarResumoDashClienteService
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.infrastructure.observabilidade.UseCaseProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class DashboardServiceConfig(
    private val proxy: UseCaseProxy,
    private val auditoria: RegistrarAuditoriaPort,
) {

    @Produces
    @ApplicationScoped
    fun buscarResumoDashClienteUseCase(
        usuarioRepositoryPort: UsuarioRepositoryPort,
        veiculoRepositoryPort: VeiculoRepositoryPort,
        ordemServicoRepositoryPort: OrdemServicoRepositoryPort,
        jwtPort: JwtPort,
    ): BuscarResumoDashClienteUseCase = proxy.envolver(
        BuscarResumoDashClienteService(
            usuarioRepositoryPort,
            veiculoRepositoryPort,
            ordemServicoRepositoryPort,
            jwtPort,
        ),
        BuscarResumoDashClienteUseCase::class.java,
        auditoria,
    )
}
