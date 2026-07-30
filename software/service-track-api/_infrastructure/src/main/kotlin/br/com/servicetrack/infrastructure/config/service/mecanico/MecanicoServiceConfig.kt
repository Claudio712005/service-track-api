package br.com.servicetrack.infrastructure.config.service.mecanico

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.mecanico.ports.`in`.AtualizarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`in`.BuscarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`in`.CadastrarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`in`.ListarMecanicosUseCase
import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.application.mecanico.service.AtualizarMecanicoService
import br.com.servicetrack.application.mecanico.service.BuscarMecanicoService
import br.com.servicetrack.application.mecanico.service.CadastrarMecanicoService
import br.com.servicetrack.application.mecanico.service.ListarMecanicosService
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.infrastructure.observabilidade.UseCaseProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class MecanicoServiceConfig(
    private val proxy: UseCaseProxy,
    private val auditoria: RegistrarAuditoriaPort,
) {

    @Produces
    @ApplicationScoped
    fun cadastrarMecanicoUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        mecanicoRepository: MecanicoRepositoryPort,
        criptografia: CriptografiaPort,
        auditoria: RegistrarAuditoriaPort
    ): CadastrarMecanicoUseCase = proxy.envolver(
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
    ): BuscarMecanicoUseCase = proxy.envolver(
        BuscarMecanicoService(mecanicoRepository, usuarioRepository),
        BuscarMecanicoUseCase::class.java,
        auditoria,
    )

    @Produces
    @ApplicationScoped
    fun listarMecanicosUseCase(
        mecanicoRepository: MecanicoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
    ): ListarMecanicosUseCase = proxy.envolver(
        ListarMecanicosService(mecanicoRepository, usuarioRepository),
        ListarMecanicosUseCase::class.java,
        auditoria,
    )

    @Produces
    @ApplicationScoped
    fun atualizarMecanicoUseCase(
        mecanicoRepository: MecanicoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
        jwt: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): AtualizarMecanicoUseCase = proxy.envolver(
        AtualizarMecanicoService(mecanicoRepository, usuarioRepository, jwt),
        AtualizarMecanicoUseCase::class.java,
        auditoria,
        antesProvider = { args -> mecanicoRepository.buscarPorId(args[0] as String) },
    )

}
