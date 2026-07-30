package br.com.servicetrack.infrastructure.config.service.insumo

import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.insumo.dto.InsumoResDTO
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
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.infrastructure.observabilidade.UseCaseProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class InsumoServiceConfig(
    private val proxy: UseCaseProxy,
    private val auditoria: RegistrarAuditoriaPort,
) {

    @Produces
    @ApplicationScoped
    fun criarInsumoUseCase(
        repository: InsumoRepositoryPort,
        auditoriaPort: RegistrarAuditoriaPort,
    ): CriarInsumoUseCase = proxy.envolver(
        CriarInsumoService(repository),
        CriarInsumoUseCase::class.java,
        auditoriaPort,
    )

    @Produces
    @ApplicationScoped
    fun buscarInsumoUseCase(
        repository: InsumoRepositoryPort
    ): BuscarInsumoUseCase = proxy.envolver(
        BuscarInsumoService(repository),
        BuscarInsumoUseCase::class.java,
        auditoria,
    )

    @Produces
    @ApplicationScoped
    fun listarInsumosUseCase(
        repository: InsumoRepositoryPort
    ): ListarInsumosUseCase = proxy.envolver(
        ListarInsumosService(repository),
        ListarInsumosUseCase::class.java,
        auditoria,
    )

    @Produces
    @ApplicationScoped
    fun atualizarInsumoUseCase(
        repository: InsumoRepositoryPort,
        auditoriaPort: RegistrarAuditoriaPort,
    ): AtualizarInsumoUseCase = proxy.envolver(
        AtualizarInsumoService(repository),
        AtualizarInsumoUseCase::class.java,
        auditoriaPort,
        antesProvider = { args -> repository.buscarPorId(args[0] as InsumoId)?.let { InsumoResDTO.de(it) } },
    )

    @Produces
    @ApplicationScoped
    fun removerInsumoUseCase(
        repository: InsumoRepositoryPort,
        auditoriaPort: RegistrarAuditoriaPort,
    ): RemoverInsumoUseCase = proxy.envolver(
        RemoverInsumoService(repository),
        RemoverInsumoUseCase::class.java,
        auditoriaPort,
        antesProvider = { args -> repository.buscarPorId(args[0] as InsumoId)?.let { InsumoResDTO.de(it) } },
    )
}
