package br.com.servicetrack.infrastructure.mecanico

import br.com.servicetrack.application.mecanico.ports.`in`.CadastrarMecanicoUseCase
import br.com.servicetrack.infrastructure.api.MecanicosApi
import br.com.servicetrack.infrastructure.api.dto.CadastrarMecanicoRequest
import jakarta.annotation.security.PermitAll
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import java.net.URI

@ApplicationScoped
class MecanicoResourceImpl @Inject constructor(
    private val cadastrarMecanicoUseCase: CadastrarMecanicoUseCase
) : MecanicosApi {

    @PermitAll
    @Transactional
    override fun criarMecanico(cadastrarMecanicoRequest: CadastrarMecanicoRequest): Response {
        val dto = cadastrarMecanicoRequest.toApplicationDTO()
        val mecanicoResDTO = cadastrarMecanicoUseCase.cadastrarMecanico(dto)
        val mecanicoResponse = mecanicoResDTO.toMecanicoResponse()
        val location = URI.create("/mecanicos/${mecanicoResDTO.usuarioId}")
        return Response.created(location)
            .entity(mecanicoResponse)
            .build()
    }
}
