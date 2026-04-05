package br.com.servicetrack.infrastructure.usuario

import br.com.servicetrack.application.usuario.ports.`in`.CriarUsuarioUseCase
import br.com.servicetrack.infrastructure.api.ClientesApi
import br.com.servicetrack.infrastructure.api.dto.CadastrarClienteRequest
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import java.net.URI

@ApplicationScoped
class ClienteResourceImpl @Inject constructor(
    private val criarUsuarioUseCase: CriarUsuarioUseCase
) : ClientesApi {

    @Transactional
    override fun criarCliente(cadastrarClienteRequest: CadastrarClienteRequest): Response {
        val command = cadastrarClienteRequest.toCommand()
        val usuarioResponse = criarUsuarioUseCase.criarUsuario(command)
        val clienteResponse = usuarioResponse.toClienteResponse()
        val location = URI.create("/clientes/${usuarioResponse.id}")
        return Response.created(location)
            .entity(clienteResponse)
            .build()
    }
}