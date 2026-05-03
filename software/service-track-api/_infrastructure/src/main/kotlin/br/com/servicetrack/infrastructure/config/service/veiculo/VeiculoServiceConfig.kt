package br.com.servicetrack.infrastructure.config.service.veiculo

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.ports.`in`.AtualizarVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.`in`.BuscarVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.`in`.CadastrarVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.`in`.ListarVeiculosUseCase
import br.com.servicetrack.application.veiculo.ports.`in`.RemoverVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.application.veiculo.service.AtualizarVeiculoService
import br.com.servicetrack.application.veiculo.service.BuscarVeiculoService
import br.com.servicetrack.application.veiculo.service.CadastrarVeiculoService
import br.com.servicetrack.application.veiculo.service.ListarVeiculosService
import br.com.servicetrack.application.veiculo.service.RemoverVeiculoService
import br.com.servicetrack.infrastructure.auditoria.AuditoriaProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class VeiculoServiceConfig {

    @Produces
    @ApplicationScoped
    fun cadastrarVeiculoUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        veiculoRepository: VeiculoRepositoryPort,
        jwt: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): CadastrarVeiculoUseCase = AuditoriaProxy.envolver(
        CadastrarVeiculoService(veiculoRepository, usuarioRepository, jwt),
        CadastrarVeiculoUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun removerVeiculoUseCase(
        jwt: JwtPort,
        repository: VeiculoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
        auditoria: RegistrarAuditoriaPort
    ): RemoverVeiculoUseCase = AuditoriaProxy.envolver(
        RemoverVeiculoService(jwt, repository, usuarioRepository),
        RemoverVeiculoUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun buscarVeiculoUseCase(
        repository: VeiculoRepositoryPort
    ): BuscarVeiculoUseCase = BuscarVeiculoService(repository)

    @Produces
    @ApplicationScoped
    fun listarVeiculosUseCase(
        repository: VeiculoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
        jwt: JwtPort
    ): ListarVeiculosUseCase = ListarVeiculosService(repository, usuarioRepository, jwt)

    @Produces
    @ApplicationScoped
    fun atualizarVeiculoUseCase(
        repository: VeiculoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
        jwt: JwtPort,
        auditoria: RegistrarAuditoriaPort
    ): AtualizarVeiculoUseCase = AuditoriaProxy.envolver(
        AtualizarVeiculoService(repository, usuarioRepository, jwt),
        AtualizarVeiculoUseCase::class.java,
        auditoria
    )

}