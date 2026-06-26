package br.com.servicetrack.infrastructure.dashboard

import br.com.servicetrack.application.dashboard.ports.`in`.BuscarResumoDashClienteUseCase
import br.com.servicetrack.infrastructure.api.DashboardApi
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.faulttolerance.Bulkhead
import org.eclipse.microprofile.faulttolerance.Timeout
import java.util.UUID

@ApplicationScoped
class DashboardResourceImpl @Inject constructor(
    private val buscarResumoDashClienteUseCase: BuscarResumoDashClienteUseCase,
) : DashboardApi {

    @RolesAllowed("CLIENTE")
    @Timeout(10000)
    @Bulkhead(10)
    override fun buscarDashboardCliente(id: UUID?): Response {
        val clienteId = id ?: return Response.status(Response.Status.BAD_REQUEST).build()
        return Response.ok(buscarResumoDashClienteUseCase.buscarResumo(clienteId.toString())).build()
    }
}
