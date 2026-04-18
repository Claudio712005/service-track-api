package br.com.servicetrack.infrastructure.config.service.usuario

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.usuario.ports.`in`.BuscarClienteUseCase
import br.com.servicetrack.application.usuario.ports.`in`.CriarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.LoginUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.usuario.service.BuscarClienteService
import br.com.servicetrack.application.usuario.service.CriarUsuarioService
import br.com.servicetrack.application.usuario.service.LoginService
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

}