package br.com.servicetrack.infrastructure.ordemServico

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
import br.com.servicetrack.infrastructure.api.OrdemServicoApi
import br.com.servicetrack.infrastructure.api.dto.AssociarItensRequest
import br.com.servicetrack.infrastructure.api.dto.CancelarOsRequest
import br.com.servicetrack.infrastructure.api.dto.ConcluirItemServicoRequest
import br.com.servicetrack.infrastructure.api.dto.GerarOrcamentoRequest
import br.com.servicetrack.infrastructure.api.dto.OrdemServicoRequest
import br.com.servicetrack.infrastructure.api.dto.ReprovarOrcamentoRequest
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate
import java.util.UUID

@ApplicationScoped
class OrdemServicoResourceImpl @Inject constructor(
    private val concluirItemServicoUseCase: ConcluirItemServicoUseCase,
    private val criarOrdemServicoUseCase: CriarOrdemServicoUseCase,
    private val enviarParaDiagnosticoUseCase: EnviarParaDiagnosticoUseCase,
    private val buscarOrdemServicoUseCase: BuscarOrdemServicoUseCase,
    private val listarOrdensServicoUseCase: ListarOrdensServicoUseCase,
    private val associarItensOrdemServicoUseCase: AssociarItensOrdemServicoUseCase,
    private val gerarOrcamentoUseCase: GerarOrcamentoUseCase,
    private val aprovarOrcamentoUseCase: AprovarOrcamentoUseCase,
    private val reprovarOrcamentoUseCase: ReprovarOrcamentoUseCase,
    private val cancelarOrdemServicoUseCase: CancelarOrdemServicoUseCase,
    private val finalizarOrdemServicoUseCase: FinalizarOrdemServicoUseCase,
    private val entregarOrdemServicoUseCase: EntregarOrdemServicoUseCase,
) : OrdemServicoApi {

    @RolesAllowed("CLIENTE", "MECANICO")
    @Transactional
    override fun criarOrdemServico(ordemServicoRequest: @Valid @NotNull OrdemServicoRequest): Response {
        val response = criarOrdemServicoUseCase.criarOrdemServico(ordemServicoRequest.toApplicationDTO())
        return Response.created(URI("/ordem-servico/${response.id}")).entity(response).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    override fun listarOrdensServico(
        status: String?,
        clienteId: UUID?,
        mecanicoId: UUID?,
        page: Int?,
        size: Int?,
    ): Response {
        val filtro = toFiltroDTO(status, clienteId?.toString(), mecanicoId?.toString(), page ?: 0, size ?: 20)
        return Response.ok(listarOrdensServicoUseCase.listarOrdensServico(filtro)).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    override fun buscarOrdemServico(id: UUID): Response {
        return Response.ok(buscarOrdemServicoUseCase.buscarOrdemServico(id.toString())).build()
    }

    @RolesAllowed("MECANICO")
    @Transactional
    override fun enviarParaDiagnostico(id: UUID): Response {
        return Response.ok(enviarParaDiagnosticoUseCase.enviarParaDiagnostico(id.toString())).build()
    }

    @RolesAllowed("MECANICO")
    @Transactional
    override fun associarItensOrdemServico(
        id: UUID,
        associarItensRequest: @Valid @NotNull AssociarItensRequest,
    ): Response {
        return Response.ok(
            associarItensOrdemServicoUseCase.associarItens(id.toString(), associarItensRequest.toApplicationDTO())
        ).build()
    }

    @RolesAllowed("MECANICO")
    @Transactional
    override fun gerarOrcamento(id: UUID, req: GerarOrcamentoRequest): Response {
        return Response.ok(gerarOrcamentoUseCase.gerarOrcamento(id.toString(), req.prazoEntrega)).build()
    }

    @RolesAllowed("CLIENTE")
    @Transactional
    override fun aprovarOrcamento(id: UUID): Response {
        return Response.ok(aprovarOrcamentoUseCase.aprovarOrcamento(id.toString())).build()
    }

    @RolesAllowed("CLIENTE")
    @Transactional
    override fun reprovarOrcamento(
        id: UUID,
        reprovarOrcamentoRequest: @Valid @NotNull ReprovarOrcamentoRequest,
    ): Response {
        return Response.ok(
            reprovarOrcamentoUseCase.reprovarOrcamento(id.toString(), reprovarOrcamentoRequest.toApplicationDTO())
        ).build()
    }

    @RolesAllowed("CLIENTE")
    @Transactional
    override fun cancelarOrdemServico(id: UUID, cancelarOsRequest: CancelarOsRequest?): Response {
        return Response.ok(
            cancelarOrdemServicoUseCase.cancelarOrdemServico(id.toString(), cancelarOsRequest.toApplicationDTO())
        ).build()
    }

    @RolesAllowed("MECANICO")
    @Transactional
    override fun finalizarOrdemServico(id: UUID): Response {
        return Response.ok(finalizarOrdemServicoUseCase.finalizarOrdemServico(id.toString())).build()
    }

    @RolesAllowed("MECANICO")
    @Transactional
    override fun entregarOrdemServico(id: UUID): Response {
        return Response.ok(entregarOrdemServicoUseCase.entregarOrdemServico(id.toString())).build()
    }

    @RolesAllowed("MECANICO")
    @Transactional
    override fun concluirItemServico(
        id: UUID,
        itemId: UUID,
        concluirItemServicoRequest: @Valid @NotNull ConcluirItemServicoRequest,
    ): Response {
        return Response.ok(
            concluirItemServicoUseCase.concluirItemServico(
                id.toString(),
                itemId.toString(),
                concluirItemServicoRequest.toApplicationDTO(),
            )
        ).build()
    }
}
