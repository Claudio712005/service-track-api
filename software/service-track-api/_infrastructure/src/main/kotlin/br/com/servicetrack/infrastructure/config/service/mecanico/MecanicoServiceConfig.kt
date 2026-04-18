package br.com.servicetrack.infrastructure.config.service.mecanico

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.mecanico.ports.`in`.BuscarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`in`.CadastrarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`in`.ListarMecanicosUseCase
import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.application.mecanico.service.BuscarMecanicoService
import br.com.servicetrack.application.mecanico.service.CadastrarMecanicoService
import br.com.servicetrack.application.mecanico.service.ListarMecanicosService
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.infrastructure.auditoria.AuditoriaProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class MecanicoServiceConfig {

    @Produces
    @ApplicationScoped
    fun cadastrarMecanicoUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        mecanicoRepository: MecanicoRepositoryPort,
        criptografia: CriptografiaPort,
        auditoria: RegistrarAuditoriaPort
    ): CadastrarMecanicoUseCase = AuditoriaProxy.envolver(
        CadastrarMecanicoService(usuarioRepository, mecanicoRepository, criptografia),
        CadastrarMecanicoUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun buscarMecanicoUseCase(
        mecanicoRepository: MecanicoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
        auditoria: RegistrarAuditoriaPort
    ): BuscarMecanicoUseCase = BuscarMecanicoService(mecanicoRepository, usuarioRepository)

    @Produces
    @ApplicationScoped
    fun listarMecanicosUseCase(
        mecanicoRepository: MecanicoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
    ): ListarMecanicosUseCase = ListarMecanicosService(mecanicoRepository, usuarioRepository)

}