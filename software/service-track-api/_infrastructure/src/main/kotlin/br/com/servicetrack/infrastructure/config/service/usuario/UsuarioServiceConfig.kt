package br.com.servicetrack.infrastructure.config.service.usuario

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.usuario.ports.`in`.AtualizarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.BuscarClienteUseCase
import br.com.servicetrack.application.usuario.ports.`in`.CriarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.DesativarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.ResetarSenhaUseCase
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.usuario.service.AtualizarUsuarioService
import br.com.servicetrack.application.usuario.service.BuscarClienteService
import br.com.servicetrack.application.usuario.service.CriarUsuarioService
import br.com.servicetrack.application.usuario.service.DesativarUsuarioService
import br.com.servicetrack.application.usuario.service.ResetarSenhaService
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.infrastructure.observabilidade.UseCaseProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class UsuarioServiceConfig(
    private val proxy: UseCaseProxy,
    private val auditoria: RegistrarAuditoriaPort,
) {

    @Produces
    @ApplicationScoped
    fun criarUsuarioUseCase(
        repository: UsuarioRepositoryPort,
        criptografia: CriptografiaPort,
        auditoria: RegistrarAuditoriaPort
    ): CriarUsuarioUseCase = proxy.envolver(
        CriarUsuarioService(repository, criptografia),
        CriarUsuarioUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun buscarClienteUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        jwt: JwtPort
    ): BuscarClienteUseCase = proxy.envolver(
        BuscarClienteService(usuarioRepository, jwt),
        BuscarClienteUseCase::class.java,
        auditoria,
    )

    @Produces
    @ApplicationScoped
    fun atualizarUsuarioUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        jwt: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): AtualizarUsuarioUseCase = proxy.envolver(
        AtualizarUsuarioService(usuarioRepository, jwt),
        AtualizarUsuarioUseCase::class.java,
        auditoria,
        antesProvider = { args -> usuarioRepository.buscarPorId(args[0] as UsuarioId) },
    )

    @Produces
    @ApplicationScoped
    fun desativarUsuarioUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        jwt: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): DesativarUsuarioUseCase = proxy.envolver(
        DesativarUsuarioService(usuarioRepository, jwt),
        DesativarUsuarioUseCase::class.java,
        auditoria,
        antesProvider = { args -> usuarioRepository.buscarPorId(args[0] as UsuarioId) },
    )

    @Produces
    @ApplicationScoped
    fun resetarSenhaUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        criptografia: CriptografiaPort,
        jwt: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): ResetarSenhaUseCase = proxy.envolver(
        ResetarSenhaService(usuarioRepository, criptografia, jwt),
        ResetarSenhaUseCase::class.java,
        auditoria
    )
}
