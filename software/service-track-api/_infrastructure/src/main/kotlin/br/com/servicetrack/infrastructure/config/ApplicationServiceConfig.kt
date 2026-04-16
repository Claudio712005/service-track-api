package br.com.servicetrack.infrastructure.config

import br.com.servicetrack.application.insumo.ports.`in`.AtualizarInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`in`.BuscarInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`in`.CriarInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`in`.ListarInsumosUseCase
import br.com.servicetrack.application.insumo.ports.`in`.RemoverInsumoUseCase
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.application.insumo.service.AtualizarInsumoService
import br.com.servicetrack.application.insumo.service.BuscarInsumoService
import br.com.servicetrack.application.insumo.service.CriarInsumoService
import br.com.servicetrack.application.insumo.service.ListarInsumosService
import br.com.servicetrack.application.insumo.service.RemoverInsumoService
import br.com.servicetrack.application.mecanico.ports.`in`.BuscarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`in`.CadastrarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`in`.ListarMecanicosUseCase
import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.application.mecanico.service.BuscarMecanicoService
import br.com.servicetrack.application.mecanico.service.CadastrarMecanicoService
import br.com.servicetrack.application.mecanico.service.ListarMecanicosService
import br.com.servicetrack.application.servico.ports.`in`.AtualizarServicoUseCase
import br.com.servicetrack.application.servico.ports.`in`.BuscarServicoUseCase
import br.com.servicetrack.application.servico.ports.`in`.CriarServicoUseCase
import br.com.servicetrack.application.servico.ports.`in`.ListarServicosUseCase
import br.com.servicetrack.application.servico.ports.`in`.RemoverServicoUseCase
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.application.servico.service.AtualizarServicoService
import br.com.servicetrack.application.servico.service.BuscarServicoService
import br.com.servicetrack.application.servico.service.CriarServicoService
import br.com.servicetrack.application.servico.service.ListarServicosService
import br.com.servicetrack.application.servico.service.RemoverServicoService
import br.com.servicetrack.application.usuario.ports.`in`.BuscarClienteUseCase
import br.com.servicetrack.application.usuario.ports.`in`.CriarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.LoginUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.usuario.service.BuscarClienteService
import br.com.servicetrack.application.usuario.service.CriarUsuarioService
import br.com.servicetrack.application.usuario.service.LoginService
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
    fun loginUsuarioUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        criptografia: CriptografiaPort,
        jwt: JwtPort
    ): LoginUsuarioUseCase = LoginService(usuarioRepository, criptografia, jwt)

    @Produces
    @ApplicationScoped
    fun buscarClienteUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        jwt: JwtPort
    ): BuscarClienteUseCase = BuscarClienteService(usuarioRepository, jwt)

    @Produces
    @ApplicationScoped
    fun cadastrarMecanicoUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        mecanicoRepository: MecanicoRepositoryPort,
        criptografia: CriptografiaPort
    ): CadastrarMecanicoUseCase = CadastrarMecanicoService(usuarioRepository, mecanicoRepository, criptografia)

    @Produces
    @ApplicationScoped
    fun buscarMecanicoUseCase(
        mecanicoRepository: MecanicoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort
    ): BuscarMecanicoUseCase = BuscarMecanicoService(mecanicoRepository, usuarioRepository)

    @Produces
    @ApplicationScoped
    fun listarMecanicosUseCase(
        mecanicoRepository: MecanicoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort
    ): ListarMecanicosUseCase = ListarMecanicosService(mecanicoRepository, usuarioRepository)

    @Produces
    @ApplicationScoped
    fun cadastrarVeiculoUseCase(
        usuarioRepository: UsuarioRepositoryPort,
        veiculoRepository: VeiculoRepositoryPort,
        jwt: JwtPort
    ): CadastrarVeiculoUseCase = CadastrarVeiculoService(veiculoRepository, usuarioRepository, jwt)

    @Produces
    @ApplicationScoped
    fun removerVeiculoUseCase(
        jwt: JwtPort,
        repository: VeiculoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort
    ): RemoverVeiculoUseCase = RemoverVeiculoService(jwt, repository, usuarioRepository)

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
        jwt: JwtPort
    ): AtualizarVeiculoUseCase = AtualizarVeiculoService(repository, usuarioRepository, jwt)

    @Produces
    @ApplicationScoped
    fun criarServicoUseCase(
        repository: ServicoRepositoryPort
    ): CriarServicoUseCase = CriarServicoService(repository)

    @Produces
    @ApplicationScoped
    fun buscarServicoUseCase(
        repository: ServicoRepositoryPort
    ): BuscarServicoUseCase = BuscarServicoService(repository)

    @Produces
    @ApplicationScoped
    fun listarServicosUseCase(
        repository: ServicoRepositoryPort
    ): ListarServicosUseCase = ListarServicosService(repository)

    @Produces
    @ApplicationScoped
    fun atualizarServicoUseCase(
        repository: ServicoRepositoryPort
    ): AtualizarServicoUseCase = AtualizarServicoService(repository)

    @Produces
    @ApplicationScoped
    fun removerServicoUseCase(
        repository: ServicoRepositoryPort
    ): RemoverServicoUseCase = RemoverServicoService(repository)

    // --- Insumo ---

    @Produces
    @ApplicationScoped
    fun criarInsumoUseCase(
        repository: InsumoRepositoryPort
    ): CriarInsumoUseCase = CriarInsumoService(repository)

    @Produces
    @ApplicationScoped
    fun buscarInsumoUseCase(
        repository: InsumoRepositoryPort
    ): BuscarInsumoUseCase = BuscarInsumoService(repository)

    @Produces
    @ApplicationScoped
    fun listarInsumosUseCase(
        repository: InsumoRepositoryPort
    ): ListarInsumosUseCase = ListarInsumosService(repository)

    @Produces
    @ApplicationScoped
    fun atualizarInsumoUseCase(
        repository: InsumoRepositoryPort
    ): AtualizarInsumoUseCase = AtualizarInsumoService(repository)

    @Produces
    @ApplicationScoped
    fun removerInsumoUseCase(
        repository: InsumoRepositoryPort
    ): RemoverInsumoUseCase = RemoverInsumoService(repository)
}
