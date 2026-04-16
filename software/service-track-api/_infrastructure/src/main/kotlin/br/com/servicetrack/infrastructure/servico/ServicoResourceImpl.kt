package br.com.servicetrack.infrastructure.servico

import br.com.servicetrack.application.servico.ports.`in`.AtualizarServicoUseCase
import br.com.servicetrack.application.servico.ports.`in`.BuscarServicoUseCase
import br.com.servicetrack.application.servico.ports.`in`.CriarServicoUseCase
import br.com.servicetrack.application.servico.ports.`in`.ListarServicosUseCase
import br.com.servicetrack.application.servico.ports.`in`.RemoverServicoUseCase
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.infrastructure.api.ServicosApi
import br.com.servicetrack.infrastructure.api.dto.AtualizarServicoRequest
import br.com.servicetrack.infrastructure.api.dto.CriarServicoRequest
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Response
import java.net.URI

@ApplicationScoped
class ServicoResourceImpl(
    private val criarServicoUseCase: CriarServicoUseCase,
    private val buscarServicoUseCase: BuscarServicoUseCase,
    private val listarServicosUseCase: ListarServicosUseCase,
    private val atualizarServicoUseCase: AtualizarServicoUseCase,
    private val removerServicoUseCase: RemoverServicoUseCase
) : ServicosApi {

    @Transactional
    @RolesAllowed("MECANICO")
    override fun criarServico(criarServicoRequest: @Valid @NotNull CriarServicoRequest): Response {
        val dto = criarServicoRequest.toApplicationDTO()
        val servico = criarServicoUseCase.criarServico(dto)
        val location = URI.create("/servicos/${servico.id}")
        return Response.created(location)
            .entity(servico.toServicoResponse())
            .build()
    }

    @RolesAllowed("MECANICO")
    override fun buscarServico(@PathParam("id") id: String): Response {
        val servico = buscarServicoUseCase.buscarServico(ServicoId(id))
        return Response.ok(servico.toServicoResponse()).build()
    }

    @RolesAllowed("MECANICO")
    override fun listarServicos(): Response {
        val servicos = listarServicosUseCase.listarServicos()
            .map { it.toServicoResponse() }
        return Response.ok(servicos).build()
    }

    @Transactional
    @RolesAllowed("MECANICO")
    override fun atualizarServico(@PathParam("id") id: String, atualizarServicoRequest: @Valid @NotNull AtualizarServicoRequest): Response {
        val dto = atualizarServicoRequest.toApplicationDTO()
        val servico = atualizarServicoUseCase.atualizarServico(ServicoId(id), dto)
        return Response.ok(servico.toServicoResponse()).build()
    }

    @Transactional
    @RolesAllowed("MECANICO")
    override fun removerServico(@PathParam("id") id: String): Response {
        removerServicoUseCase.removerServico(ServicoId(id))
        return Response.noContent().build()
    }
}
