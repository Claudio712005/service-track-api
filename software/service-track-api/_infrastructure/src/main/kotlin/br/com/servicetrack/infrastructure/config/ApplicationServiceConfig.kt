package br.com.servicetrack.infrastructure.config

import br.com.servicetrack.application.mecanico.ports.`in`.CadastrarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.application.mecanico.service.CadastrarMecanicoService
import br.com.servicetrack.application.usuario.ports.`in`.CriarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.LoginUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.usuario.service.CriarUsuarioService
import br.com.servicetrack.application.usuario.service.LoginService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class ApplicationServiceConfig {

    @Produces
    @ApplicationScoped
    fun criarUsuarioUseCase(
        repository: UsuarioRepositoryPort,
        criptografia: CriptografiaPort
    ): CriarUsuarioUseCase = CriarUsuarioService(repository, criptografia)

    @Produces
    @ApplicationScoped
    fun cadastrarMecanicoUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        mecanicoRepository: MecanicoRepositoryPort,
        criptografia: CriptografiaPort
    ): CadastrarMecanicoUseCase = CadastrarMecanicoService(usuarioRepository, mecanicoRepository, criptografia)

    @Produces
    @ApplicationScoped
    fun loginUsuarioUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        criptografia: CriptografiaPort,
        jwt: JwtPort
    ): LoginUsuarioUseCase = LoginService(usuarioRepository, criptografia, jwt)
}
