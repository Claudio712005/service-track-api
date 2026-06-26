package br.com.servicetrack.infrastructure.autenticacao

import br.com.servicetrack.application.usuario.ports.`in`.LoginUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`in`.ResetarSenhaUseCase
import br.com.servicetrack.infrastructure.api.AutenticacaoApi
import br.com.servicetrack.infrastructure.api.dto.LoginRequest
import br.com.servicetrack.infrastructure.api.dto.ResetarSenhaRequest
import io.smallrye.faulttolerance.api.RateLimit
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.faulttolerance.Timeout
import java.time.temporal.ChronoUnit

@ApplicationScoped
class AutenticacaoResourceImpl @Inject constructor(
    private val loginUseCase: LoginUsuarioUseCase,
    private val resetarSenhaUseCase: ResetarSenhaUseCase,
) : AutenticacaoApi {

    @PermitAll
    @Timeout(3000)
    @RateLimit(value = 20, window = 1, windowUnit = ChronoUnit.MINUTES)
    override fun login(loginRequest: LoginRequest): Response {
        val dto = loginRequest.toApplicationDTO()
        val resultado = loginUseCase.login(dto)
        return Response.ok(resultado.toLoginResponse()).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    @Transactional
    @Timeout(3000)
    @RateLimit(value = 5, window = 1, windowUnit = ChronoUnit.MINUTES)
    override fun resetarSenha(resetarSenhaRequest: ResetarSenhaRequest): Response {
        val dto = resetarSenhaRequest.toApplicationDTO()
        resetarSenhaUseCase.resetarSenha(dto)
        return Response.noContent().build()
    }
}
