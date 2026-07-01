package br.com.servicetrack.infrastructure.config.service.ordemServico

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.ordemServico.ports.`in`.AprovarOrcamentoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.ConcluirItemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.AssociarItensOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.BuscarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.CancelarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.CriarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.EntregarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.EnviarParaDiagnosticoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.FinalizarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.GerarOrcamentoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.ListarOrdensServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`in`.ReprovarOrcamentoUseCase
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.service.AprovarOrcamentoService
import br.com.servicetrack.application.ordemServico.service.ConcluirItemServicoService
import br.com.servicetrack.application.ordemServico.service.AssociarItensOrdemServicoService
import br.com.servicetrack.application.ordemServico.service.BuscarOrdemServicoService
import br.com.servicetrack.application.ordemServico.service.CancelarOrdemServicoService
import br.com.servicetrack.application.ordemServico.service.CriarOrdemServicoService
import br.com.servicetrack.application.ordemServico.service.EntregarOrdemServicoService
import br.com.servicetrack.application.ordemServico.service.EnviarParaDiagnosticoService
import br.com.servicetrack.application.ordemServico.service.FinalizarOrdemServicoService
import br.com.servicetrack.application.ordemServico.service.GerarOrcamentoService
import br.com.servicetrack.application.ordemServico.service.ListarOrdensServicoService
import br.com.servicetrack.application.ordemServico.service.ReprovarOrcamentoService
import br.com.servicetrack.application.servico.ports.`out`.ServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.infrastructure.auditoria.AuditoriaProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Event
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class OrdemServicoServiceConfig {

    @Produces
    @ApplicationScoped
    fun criarOrdemServicoUseCase(
        repository: OrdemServicoRepositoryPort,
        usuarioRepositoryPort: UsuarioRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort,
    ): CriarOrdemServicoUseCase = AuditoriaProxy.envolver(
        CriarOrdemServicoService(repository, usuarioRepositoryPort, jwtPort),
        CriarOrdemServicoUseCase::class.java,
        auditoria,
    )

    @Produces
    @ApplicationScoped
    fun enviarParaDiagnosticoUseCase(
        repository: OrdemServicoRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort,
        statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
    ): EnviarParaDiagnosticoUseCase = AuditoriaProxy.envolver(
        EnviarParaDiagnosticoService(repository, jwtPort, statusAlteradoEvent),
        EnviarParaDiagnosticoUseCase::class.java,
        auditoria,
        antesProvider = { args -> repository.buscarPorId(OrdemServicoId(args[0] as String)) },
    )

    @Produces
    @ApplicationScoped
    fun buscarOrdemServicoUseCase(
        repository: OrdemServicoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
        jwtPort: JwtPort,
    ): BuscarOrdemServicoUseCase = BuscarOrdemServicoService(repository, usuarioRepository, jwtPort)

    @Produces
    @ApplicationScoped
    fun listarOrdensServicoUseCase(
        repository: OrdemServicoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
        jwtPort: JwtPort,
    ): ListarOrdensServicoUseCase = ListarOrdensServicoService(repository, usuarioRepository, jwtPort)

    @Produces
    @ApplicationScoped
    fun associarItensOrdemServicoUseCase(
        osRepository: OrdemServicoRepositoryPort,
        servicoRepository: ServicoRepositoryPort,
        insumoRepository: InsumoRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort,
    ): AssociarItensOrdemServicoUseCase = AuditoriaProxy.envolver(
        AssociarItensOrdemServicoService(osRepository, servicoRepository, insumoRepository, jwtPort),
        AssociarItensOrdemServicoUseCase::class.java,
        auditoria,
        antesProvider = { args -> osRepository.buscarPorId(OrdemServicoId(args[0] as String)) },
    )

    @Produces
    @ApplicationScoped
    fun gerarOrcamentoUseCase(
        osRepository: OrdemServicoRepositoryPort,
        insumoRepository: InsumoRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort,
        statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
    ): GerarOrcamentoUseCase = AuditoriaProxy.envolver(
        GerarOrcamentoService(osRepository, insumoRepository, jwtPort, statusAlteradoEvent),
        GerarOrcamentoUseCase::class.java,
        auditoria,
        antesProvider = { args -> osRepository.buscarPorId(OrdemServicoId(args[0] as String)) },
    )

    @Produces
    @ApplicationScoped
    fun aprovarOrcamentoUseCase(
        repository: OrdemServicoRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort,
        statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
    ): AprovarOrcamentoUseCase = AuditoriaProxy.envolver(
        AprovarOrcamentoService(repository, jwtPort, statusAlteradoEvent),
        AprovarOrcamentoUseCase::class.java,
        auditoria,
        antesProvider = { args -> repository.buscarPorId(OrdemServicoId(args[0] as String)) },
    )

    @Produces
    @ApplicationScoped
    fun reprovarOrcamentoUseCase(
        osRepository: OrdemServicoRepositoryPort,
        insumoRepository: InsumoRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort,
        statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
    ): ReprovarOrcamentoUseCase = AuditoriaProxy.envolver(
        ReprovarOrcamentoService(osRepository, insumoRepository, jwtPort, statusAlteradoEvent),
        ReprovarOrcamentoUseCase::class.java,
        auditoria,
        antesProvider = { args -> osRepository.buscarPorId(OrdemServicoId(args[0] as String)) },
    )

    @Produces
    @ApplicationScoped
    fun cancelarOrdemServicoUseCase(
        osRepository: OrdemServicoRepositoryPort,
        insumoRepository: InsumoRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort,
        statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
    ): CancelarOrdemServicoUseCase = AuditoriaProxy.envolver(
        CancelarOrdemServicoService(osRepository, insumoRepository, jwtPort, statusAlteradoEvent),
        CancelarOrdemServicoUseCase::class.java,
        auditoria,
        antesProvider = { args -> osRepository.buscarPorId(OrdemServicoId(args[0] as String)) },
    )

    @Produces
    @ApplicationScoped
    fun finalizarOrdemServicoUseCase(
        osRepository: OrdemServicoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort,
        statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
    ): FinalizarOrdemServicoUseCase = AuditoriaProxy.envolver(
        FinalizarOrdemServicoService(osRepository, usuarioRepository, jwtPort, statusAlteradoEvent),
        FinalizarOrdemServicoUseCase::class.java,
        auditoria,
        antesProvider = { args -> osRepository.buscarPorId(OrdemServicoId(args[0] as String)) },
    )

    @Produces
    @ApplicationScoped
    fun entregarOrdemServicoUseCase(
        osRepository: OrdemServicoRepositoryPort,
        usuarioRepository: UsuarioRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort,
        statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
    ): EntregarOrdemServicoUseCase = AuditoriaProxy.envolver(
        EntregarOrdemServicoService(osRepository, usuarioRepository, jwtPort, statusAlteradoEvent),
        EntregarOrdemServicoUseCase::class.java,
        auditoria,
        antesProvider = { args -> osRepository.buscarPorId(OrdemServicoId(args[0] as String)) },
    )

    @Produces
    @ApplicationScoped
    fun concluirItemServicoUseCase(
        osRepository: OrdemServicoRepositoryPort,
        jwtPort: JwtPort,
        auditoria: RegistrarAuditoriaPort,
    ): ConcluirItemServicoUseCase = AuditoriaProxy.envolver(
        ConcluirItemServicoService(osRepository, jwtPort),
        ConcluirItemServicoUseCase::class.java,
        auditoria,
        antesProvider = { args -> osRepository.buscarPorId(OrdemServicoId(args[0] as String)) },
    )
}
