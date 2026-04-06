package br.com.servicetrack.infrastructure.veiculo

import br.com.servicetrack.application.veiculo.ports.`in`.CadastrarVeiculoUseCase
import br.com.servicetrack.infrastructure.api.VeiculosApi
import br.com.servicetrack.infrastructure.api.dto.CadastrarVeiculoRequest
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import java.net.URI

@ApplicationScoped
class VeiculoResourceImpl(
    private val cadastrarVeiculoUseCase: CadastrarVeiculoUseCase,
    private val jwt: JsonWebToken
) : VeiculosApi {

    @Transactional
    @RolesAllowed("CLIENTE", "MECANICO")
    override fun criarVeiculo(cadastrarVeiculoRequest: @Valid @NotNull CadastrarVeiculoRequest): Response {
        val usuarioId = jwt.subject
        val dto = cadastrarVeiculoRequest.toApplicationDTO()
        val veiculoResponse = cadastrarVeiculoUseCase.cadastrarVeiculo(dto, usuarioId)
        val location = URI.create("/veiculos/${veiculoResponse.id}")
        return Response.created(location)
            .entity(veiculoResponse)
            .build()
    }
}
