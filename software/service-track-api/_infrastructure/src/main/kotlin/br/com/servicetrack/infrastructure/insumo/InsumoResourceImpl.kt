package br.com.servicetrack.infrastructure.insumo

import br.com.servicetrack.application.insumo.ports.`in`.AtualizarInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`in`.BuscarInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`in`.CriarInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`in`.ListarInsumosUseCase
import br.com.servicetrack.application.insumo.ports.`in`.RemoverInsumoUseCase
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.infrastructure.api.InsumosApi
import br.com.servicetrack.infrastructure.api.dto.AtualizarInsumoRequest
import br.com.servicetrack.infrastructure.api.dto.CriarInsumoRequest
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.faulttolerance.Timeout
import java.net.URI

@ApplicationScoped
class InsumoResourceImpl(
    private val criarInsumoUseCase: CriarInsumoUseCase,
    private val buscarInsumoUseCase: BuscarInsumoUseCase,
    private val listarInsumosUseCase: ListarInsumosUseCase,
    private val atualizarInsumoUseCase: AtualizarInsumoUseCase,
    private val removerInsumoUseCase: RemoverInsumoUseCase,
) : InsumosApi {

    @Transactional
    @RolesAllowed("MECANICO")
    @Timeout(3000)
    override fun criarInsumo(criarInsumoRequest: @Valid @NotNull CriarInsumoRequest): Response {
        val dto = criarInsumoRequest.toApplicationDTO()
        val insumo = criarInsumoUseCase.criarInsumo(dto)
        val location = URI.create("/insumos/${insumo.id}")
        return Response.created(location)
            .entity(insumo.toInsumoResponse())
            .build()
    }

    @RolesAllowed("MECANICO")
    @Timeout(2000)
    override fun buscarInsumo(@PathParam("id") id: String): Response {
        val insumo = buscarInsumoUseCase.buscarInsumo(InsumoId.de(id))
        return Response.ok(insumo.toInsumoResponse()).build()
    }

    @RolesAllowed("MECANICO")
    @Timeout(5000)
    override fun listarInsumos(): Response {
        val insumos = listarInsumosUseCase.listarInsumos()
            .map { it.toInsumoResponse() }
        return Response.ok(insumos).build()
    }

    @Transactional
    @RolesAllowed("MECANICO")
    @Timeout(3000)
    override fun atualizarInsumo(@PathParam("id") id: String, atualizarInsumoRequest: @Valid @NotNull AtualizarInsumoRequest): Response {
        val dto = atualizarInsumoRequest.toApplicationDTO()
        val insumo = atualizarInsumoUseCase.atualizarInsumo(InsumoId.de(id), dto)
        return Response.ok(insumo.toInsumoResponse()).build()
    }

    @Transactional
    @RolesAllowed("MECANICO")
    @Timeout(3000)
    override fun removerInsumo(@PathParam("id") id: String): Response {
        removerInsumoUseCase.removerInsumo(InsumoId.de(id))
        return Response.noContent().build()
    }
}
