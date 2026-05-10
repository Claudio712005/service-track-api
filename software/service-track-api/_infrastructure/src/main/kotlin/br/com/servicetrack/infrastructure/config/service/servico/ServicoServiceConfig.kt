package br.com.servicetrack.infrastructure.config.service.servico

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.ordemServico.ports.out.ItemOrdemServicoRepositoryPort
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
import br.com.servicetrack.infrastructure.auditoria.AuditoriaProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class ServicoServiceConfig {

    @Produces
    @ApplicationScoped
    fun criarServicoUseCase(
        repository: ServicoRepositoryPort,
        auditoria: RegistrarAuditoriaPort
    ): CriarServicoUseCase = AuditoriaProxy.envolver(
        CriarServicoService(repository),
        CriarServicoUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun buscarServicoUseCase(
        repository: ServicoRepositoryPort,
    ): BuscarServicoUseCase = BuscarServicoService(repository)

    @Produces
    @ApplicationScoped
    fun listarServicosUseCase(
        repository: ServicoRepositoryPort
    ): ListarServicosUseCase = ListarServicosService(repository)

    @Produces
    @ApplicationScoped
    fun atualizarServicoUseCase(
        repository: ServicoRepositoryPort,
        auditoria: RegistrarAuditoriaPort
    ): AtualizarServicoUseCase = AuditoriaProxy.envolver(
        AtualizarServicoService(repository),
        AtualizarServicoUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun removerServicoUseCase(
        repository: ServicoRepositoryPort,
        auditoria: RegistrarAuditoriaPort
    ): RemoverServicoUseCase = AuditoriaProxy.envolver(
        RemoverServicoService(repository),
        RemoverServicoUseCase::class.java,
        auditoria
    )

    @Produces
    @ApplicationScoped
    fun buscarTempoMedioConclusaoUseCase(
        servicoRepository: ServicoRepositoryPort,
        itemOrdemServicoRepository: ItemOrdemServicoRepositoryPort,
    ): BuscarTempoMedioConclusaoUseCase = BuscarTempoMedioConclusaoService(servicoRepository, itemOrdemServicoRepository)

}