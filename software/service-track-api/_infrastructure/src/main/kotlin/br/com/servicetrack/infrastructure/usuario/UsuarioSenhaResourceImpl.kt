package br.com.servicetrack.infrastructure.usuario

import br.com.servicetrack.application.usuario.dto.request.ResetarSenhaReqDTO
import br.com.servicetrack.application.usuario.ports.`in`.ResetarSenhaUseCase
import br.com.servicetrack.infrastructure.api.UsuariosApi
import br.com.servicetrack.infrastructure.api.dto.AlterarSenhaRequest
import io.smallrye.faulttolerance.api.RateLimit
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.faulttolerance.Timeout
import java.time.temporal.ChronoUnit

@ApplicationScoped
class UsuarioSenhaResourceImpl @Inject constructor(
    private val resetarSenhaUseCase: ResetarSenhaUseCase,
) : UsuariosApi {

    @RolesAllowed("CLIENTE", "MECANICO")
    @Transactional
    @Timeout(3000)
    @RateLimit(value = 5, window = 1, windowUnit = ChronoUnit.MINUTES)
    override fun alterarSenha(alterarSenhaRequest: AlterarSenhaRequest): Response {
        resetarSenhaUseCase.resetarSenha(
            ResetarSenhaReqDTO(
                senhaAtual = alterarSenhaRequest.senhaAtual,
                novaSenha = alterarSenhaRequest.novaSenha,
                confirmacaoNovaSenha = alterarSenhaRequest.confirmacaoNovaSenha,
            )
        )
        return Response.noContent().build()
    }
}
