package br.com.servicetrack.infrastructure.autenticacao

import br.com.servicetrack.application.usuario.ports.`in`.LoginUsuarioUseCase
import br.com.servicetrack.infrastructure.api.AutenticacaoApi
import br.com.servicetrack.infrastructure.api.dto.LoginRequest
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response

@ApplicationScoped
class AutenticacaoResourceImpl @Inject constructor(
    private val loginUseCase: LoginUsuarioUseCase
) : AutenticacaoApi {

    override fun login(loginRequest: LoginRequest): Response {
        val dto = loginRequest.toApplicationDTO()
        val resultado = loginUseCase.login(dto)
        return Response.ok(resultado.toLoginResponse()).build()
    }
}
