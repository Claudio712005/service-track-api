package br.com.servicetrack.infrastructure.ordemServico

import br.com.servicetrack.application.ordemServico.ports.`in`.CriarOrdemServicoUseCase
import br.com.servicetrack.infrastructure.api.OrdemServicoApi
import br.com.servicetrack.infrastructure.api.dto.OrdemServicoRequest
import jakarta.annotation.security.PermitAll
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.core.Response
import java.net.URI

@ApplicationScoped
class OrdemServicoResourceImpl @Inject constructor(
    private val criarOrdemServicoUseCase: CriarOrdemServicoUseCase
): OrdemServicoApi {

    @PermitAll
    @Transactional
    override fun criarOrdemServico(ordemServicoRequest: @Valid @NotNull OrdemServicoRequest): Response? {
        val dto = ordemServicoRequest.toApplicationDTO()
        val response = criarOrdemServicoUseCase.criarOrdemServico(dto)

        return Response.created(URI("/ordens-servico/${response.id}")).build()
    }


}