package br.com.servicetrack.infrastructure.catalogo

import br.com.servicetrack.application.insumo.ports.`in`.ListarInsumosUseCase
import br.com.servicetrack.application.servico.ports.`in`.ListarServicosUseCase
import br.com.servicetrack.infrastructure.api.CatalogoApi
import br.com.servicetrack.infrastructure.insumo.toCatalogoInsumoResponse
import br.com.servicetrack.infrastructure.servico.toCatalogoServicoResponse
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.Response

@ApplicationScoped
class CatalogoResourceImpl(
    private val listarServicosUseCase: ListarServicosUseCase,
    private val listarInsumosUseCase: ListarInsumosUseCase
) : CatalogoApi {

    @RolesAllowed("CLIENTE", "MECANICO")
    override fun listarCatalogoServicos(): Response {
        val servicos = listarServicosUseCase.listarResumidos()
            .map { it.toCatalogoServicoResponse() }
        return Response.ok(servicos).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    override fun listarCatalogoInsumos(): Response {
        val insumos = listarInsumosUseCase.listarResumidos()
            .map { it.toCatalogoInsumoResponse() }
        return Response.ok(insumos).build()
    }
}
