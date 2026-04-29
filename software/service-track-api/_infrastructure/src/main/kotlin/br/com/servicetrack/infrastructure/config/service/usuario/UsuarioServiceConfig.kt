package br.com.servicetrack.infrastructure.config.service.usuario

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.usuario.ports.`in`.AtualizarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.BuscarClienteUseCase
import br.com.servicetrack.application.usuario.ports.`in`.CriarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.DesativarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.LoginUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.ResetarSenhaUseCase
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.usuario.service.AtualizarUsuarioService
import br.com.servicetrack.application.usuario.service.BuscarClienteService
import br.com.servicetrack.application.usuario.service.CriarUsuarioService
import br.com.servicetrack.application.usuario.service.DesativarUsuarioService
import br.com.servicetrack.application.usuario.service.LoginService
import br.com.servicetrack.application.usuario.service.ResetarSenhaService
import br.com.servicetrack.infrastructure.auditoria.AuditoriaProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class UsuarioServiceConfig {

    @Produces
    @ApplicationScoped
    fun criarUsuarioUseCase(
        repository: UsuarioRepositoryPort,
        criptografia: CriptografiaPort,
        auditoria: RegistrarAuditoriaPort
    ): CriarUsuarioUseCase = AuditoriaProxy.envolver(
        CriarUsuarioService(repository, criptografia),
        CriarUsuarioUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun loginUsuarioUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        criptografia: CriptografiaPort,
        jwt: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): LoginUsuarioUseCase = AuditoriaProxy.envolver(
        LoginService(usuarioRepository, criptografia, jwt),
        LoginUsuarioUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun buscarClienteUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        jwt: JwtPort
    ): BuscarClienteUseCase = BuscarClienteService(usuarioRepository, jwt)

    @Produces
    @ApplicationScoped
    fun atualizarUsuarioUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        jwt: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): AtualizarUsuarioUseCase = AuditoriaProxy.envolver(
        AtualizarUsuarioService(usuarioRepository, jwt),
        AtualizarUsuarioUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun desativarUsuarioUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        jwt: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): DesativarUsuarioUseCase = AuditoriaProxy.envolver(
        DesativarUsuarioService(usuarioRepository, jwt),
        DesativarUsuarioUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun resetarSenhaUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        criptografia: CriptografiaPort,
        jwt: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): ResetarSenhaUseCase = AuditoriaProxy.envolver(
        ResetarSenhaService(usuarioRepository, criptografia, jwt),
        ResetarSenhaUseCase::class.java,
        auditoria
    )
}
