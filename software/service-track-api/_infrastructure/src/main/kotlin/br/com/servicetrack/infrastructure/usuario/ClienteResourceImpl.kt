package br.com.servicetrack.infrastructure.usuario

import br.com.servicetrack.application.usuario.ports.`in`.AtualizarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.BuscarClienteUseCase
import br.com.servicetrack.application.usuario.ports.`in`.CriarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.DesativarUsuarioUseCase
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.infrastructure.api.ClientesApi
import br.com.servicetrack.infrastructure.api.dto.AtualizarClienteRequest
import br.com.servicetrack.infrastructure.api.dto.CadastrarClienteRequest
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Response
import java.net.URI

@ApplicationScoped
class ClienteResourceImpl @Inject constructor(
    private val criarUsuarioUseCase: CriarUsuarioUseCase,
    private val buscarClienteUseCase: BuscarClienteUseCase,
    private val atualizarUsuarioUseCase: AtualizarUsuarioUseCase,
    private val desativarUsuarioUseCase: DesativarUsuarioUseCase
) : ClientesApi {

    @PermitAll
    @Transactional
    override fun criarCliente(cadastrarClienteRequest: CadastrarClienteRequest): Response {
        val dto = cadastrarClienteRequest.toApplicationDTO()
        val usuarioResponse = criarUsuarioUseCase.criarUsuario(dto)
        val clienteResponse = usuarioResponse.toClienteResponse()
        val location = URI.create("/clientes/${usuarioResponse.id}")
        return Response.created(location)
            .entity(clienteResponse)
            .build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    override fun buscarCliente(@PathParam("id") id: String): Response {
        val cliente = buscarClienteUseCase.buscarCliente(UsuarioId(id))
        return Response.ok(cliente.toClienteResponse()).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    @Transactional
    override fun atualizarCliente(@PathParam("id") id: String, atualizarClienteRequest: AtualizarClienteRequest): Response {
        val dto = atualizarClienteRequest.toApplicationDTO()
        val resultado = atualizarUsuarioUseCase.atualizarUsuario(UsuarioId(id), dto)
        return Response.ok(resultado.toClienteResponse()).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    @Transactional
    override fun desativarCliente(@PathParam("id") id: String): Response {
        desativarUsuarioUseCase.desativarUsuario(UsuarioId(id))
        return Response.noContent().build()
    }
}
