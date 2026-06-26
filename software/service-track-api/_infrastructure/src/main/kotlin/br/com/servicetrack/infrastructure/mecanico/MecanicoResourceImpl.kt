package br.com.servicetrack.infrastructure.mecanico

import br.com.servicetrack.application.mecanico.ports.`in`.AtualizarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`in`.BuscarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`in`.CadastrarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`in`.ListarMecanicosUseCase
import br.com.servicetrack.infrastructure.api.MecanicosApi
import br.com.servicetrack.infrastructure.api.dto.AtualizarMecanicoRequest
import br.com.servicetrack.infrastructure.api.dto.CadastrarMecanicoRequest
import io.smallrye.faulttolerance.api.RateLimit
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.faulttolerance.Timeout
import java.net.URI
import java.time.temporal.ChronoUnit

@ApplicationScoped
class MecanicoResourceImpl @Inject constructor(
    private val cadastrarMecanicoUseCase: CadastrarMecanicoUseCase,
    private val buscarMecanicoUseCase: BuscarMecanicoUseCase,
    private val listarMecanicosUseCase: ListarMecanicosUseCase,
    private val atualizarMecanicoUseCase: AtualizarMecanicoUseCase,
) : MecanicosApi {

    @PermitAll
    @Transactional
    @Timeout(3000)
    @RateLimit(value = 10, window = 1, windowUnit = ChronoUnit.MINUTES)
    override fun criarMecanico(cadastrarMecanicoRequest: CadastrarMecanicoRequest): Response {
        val dto = cadastrarMecanicoRequest.toApplicationDTO()
        val mecanicoResDTO = cadastrarMecanicoUseCase.cadastrarMecanico(dto)
        val mecanicoResponse = mecanicoResDTO.toMecanicoResponse()
        val location = URI.create("/mecanicos/${mecanicoResDTO.usuarioId}")
        return Response.created(location)
            .entity(mecanicoResponse)
            .build()
    }

    @RolesAllowed("MECANICO")
    @Timeout(2000)
    override fun buscarMecanico(@PathParam("id") id: String): Response {
        val mecanico = buscarMecanicoUseCase.buscarMecanico(id)
        return Response.ok(mecanico.toMecanicoResponse()).build()
    }

    @RolesAllowed("MECANICO")
    @Timeout(5000)
    override fun listarMecanicos(): Response {
        val mecanicos = listarMecanicosUseCase.listarMecanicos()
            .map { it.toMecanicoResponse() }
        return Response.ok(mecanicos).build()
    }

    @RolesAllowed("MECANICO")
    @Transactional
    @Timeout(3000)
    override fun atualizarMecanico(@PathParam("id") id: String, atualizarMecanicoRequest: AtualizarMecanicoRequest): Response {
        val dto = atualizarMecanicoRequest.toApplicationDTO()
        val mecanicoResDTO = atualizarMecanicoUseCase.atualizarMecanico(id, dto)
        return Response.ok(mecanicoResDTO.toMecanicoResponse()).build()
    }
}
