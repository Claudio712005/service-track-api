package br.com.servicetrack.infrastructure.config.service.servico

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.ordemServico.ports.out.ItemOrdemServicoRepositoryPort
import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.application.servico.ports.`in`.AtualizarServicoUseCase
import br.com.servicetrack.application.servico.ports.`in`.BuscarServicoUseCase
import br.com.servicetrack.application.servico.ports.`in`.BuscarTempoMedioConclusaoUseCase
import br.com.servicetrack.application.servico.ports.`in`.CriarServicoUseCase
import br.com.servicetrack.application.servico.ports.`in`.ListarServicosUseCase
import br.com.servicetrack.application.servico.ports.`in`.RemoverServicoUseCase
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.application.servico.service.AtualizarServicoService
import br.com.servicetrack.application.servico.service.BuscarServicoService
import br.com.servicetrack.application.servico.service.BuscarTempoMedioConclusaoService
import br.com.servicetrack.application.servico.service.CriarServicoService
import br.com.servicetrack.application.servico.service.ListarServicosService
import br.com.servicetrack.application.servico.service.RemoverServicoService
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.infrastructure.observabilidade.UseCaseProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class ServicoServiceConfig(
    private val proxy: UseCaseProxy,
    private val auditoria: RegistrarAuditoriaPort,
) {

    @Produces
    @ApplicationScoped
    fun criarServicoUseCase(
        repository: ServicoRepositoryPort,
        auditoria: RegistrarAuditoriaPort
    ): CriarServicoUseCase = proxy.envolver(
        CriarServicoService(repository),
        CriarServicoUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun buscarServicoUseCase(
        repository: ServicoRepositoryPort,
    ): BuscarServicoUseCase = proxy.envolver(
        BuscarServicoService(repository),
        BuscarServicoUseCase::class.java,
        auditoria,
    )

    @Produces
    @ApplicationScoped
    fun listarServicosUseCase(
        repository: ServicoRepositoryPort
    ): ListarServicosUseCase = proxy.envolver(
        ListarServicosService(repository),
        ListarServicosUseCase::class.java,
        auditoria,
    )

    @Produces
    @ApplicationScoped
    fun atualizarServicoUseCase(
        repository: ServicoRepositoryPort,
        auditoria: RegistrarAuditoriaPort
    ): AtualizarServicoUseCase = proxy.envolver(
        AtualizarServicoService(repository),
        AtualizarServicoUseCase::class.java,
        auditoria,
        antesProvider = { args -> repository.buscarPorId(args[0] as ServicoId)?.let { ServicoResDTO.de(it) } },
    )

    @Produces
    @ApplicationScoped
    fun removerServicoUseCase(
        repository: ServicoRepositoryPort,
        auditoria: RegistrarAuditoriaPort
    ): RemoverServicoUseCase = proxy.envolver(
        RemoverServicoService(repository),
        RemoverServicoUseCase::class.java,
        auditoria,
        antesProvider = { args -> repository.buscarPorId(args[0] as ServicoId)?.let { ServicoResDTO.de(it) } },
    )

    @Produces
    @ApplicationScoped
    fun buscarTempoMedioConclusaoUseCase(
        servicoRepository: ServicoRepositoryPort,
        itemOrdemServicoRepository: ItemOrdemServicoRepositoryPort,
    ): BuscarTempoMedioConclusaoUseCase = proxy.envolver(
        BuscarTempoMedioConclusaoService(servicoRepository, itemOrdemServicoRepository),
        BuscarTempoMedioConclusaoUseCase::class.java,
        auditoria,
    )

}
