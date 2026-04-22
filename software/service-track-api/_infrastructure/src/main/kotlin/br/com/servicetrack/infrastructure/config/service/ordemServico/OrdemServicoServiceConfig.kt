package br.com.servicetrack.infrastructure.config.service.ordemServico

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.ordemServico.ports.`in`.CriarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.service.CriarOrdemServicoService
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.infrastructure.auditoria.AuditoriaProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class OrdemServicoServiceConfig {

    @Produces
    @ApplicationScoped
    fun criarOrdemServicoUseCase(
        repository: OrdemServicoRepositoryPort,
        usuarioRepositoryPort: UsuarioRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): CriarOrdemServicoUseCase = AuditoriaProxy.envolver(
        CriarOrdemServicoService(repository, usuarioRepositoryPort, jwtPort),
        CriarOrdemServicoUseCase::class.java,
        auditoria
    )
}