package br.com.servicetrack.infrastructure.veiculo

import br.com.servicetrack.application.veiculo.ports.`in`.AtualizarVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.`in`.BuscarSugestoesImagensUseCase
import br.com.servicetrack.application.veiculo.ports.`in`.BuscarVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.`in`.CadastrarVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.`in`.ListarVeiculosUseCase
import br.com.servicetrack.application.veiculo.ports.`in`.RemoverVeiculoUseCase
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import br.com.servicetrack.infrastructure.api.VeiculosApi
import br.com.servicetrack.infrastructure.api.dto.AtualizarVeiculoRequest
import br.com.servicetrack.infrastructure.api.dto.CadastrarVeiculoRequest
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.faulttolerance.Bulkhead
import org.eclipse.microprofile.faulttolerance.Timeout
import java.net.URI

@ApplicationScoped
class VeiculoResourceImpl(
    private val cadastrarVeiculoUseCase: CadastrarVeiculoUseCase,
    private val removerVeiculoUseCase: RemoverVeiculoUseCase,
    private val buscarVeiculoUseCase: BuscarVeiculoUseCase,
    private val listarVeiculosUseCase: ListarVeiculosUseCase,
    private val atualizarVeiculoUseCase: AtualizarVeiculoUseCase,
    private val buscarSugestoesImagensUseCase: BuscarSugestoesImagensUseCase,
) : VeiculosApi {

    @Transactional
    @RolesAllowed("CLIENTE", "MECANICO")
    @Timeout(15000)
    @Bulkhead(10)
    override fun criarVeiculo(cadastrarVeiculoRequest: @Valid @NotNull CadastrarVeiculoRequest): Response {
        val dto = cadastrarVeiculoRequest.toApplicationDTO()
        val veiculoResponse = cadastrarVeiculoUseCase.cadastrarVeiculo(dto)
        val location = URI.create("/veiculos/${veiculoResponse.id}")
        return Response.created(location)
            .entity(veiculoResponse.toDadosVeiculoResponse())
            .build()
    }

    @Transactional
    @RolesAllowed("CLIENTE", "MECANICO")
    @Timeout(3000)
    override fun excluirVeiculo(@PathParam("id") id: String): Response {
        removerVeiculoUseCase.removerVeiculo(VeiculoId(id))
        return Response.noContent().build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    @Timeout(2000)
    override fun buscarVeiculo(@PathParam("id") id: String): Response {
        val veiculo = buscarVeiculoUseCase.buscarVeiculo(VeiculoId(id))
        return Response.ok(veiculo.toDadosVeiculoResponse()).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    @Timeout(5000)
    override fun listarVeiculos(): Response {
        val veiculos = listarVeiculosUseCase.listarVeiculos()
            .map { it.toDadosVeiculoResponse() }
        return Response.ok(veiculos).build()
    }

    @Transactional
    @RolesAllowed("CLIENTE", "MECANICO")
    @Timeout(5000)
    override fun atualizarVeiculo(@PathParam("id") id: String, atualizarVeiculoRequest: @Valid @NotNull AtualizarVeiculoRequest): Response {
        val dto = atualizarVeiculoRequest.toApplicationDTO()
        val veiculo = atualizarVeiculoUseCase.atualizarVeiculo(VeiculoId(id), dto)
        return Response.ok(veiculo.toDadosVeiculoResponse()).build()
    }

    @RolesAllowed("CLIENTE", "MECANICO")
    @Timeout(12000)
    @Bulkhead(5)
    override fun buscarSugestoesImagens(
        @QueryParam("marca") marca: String,
        @QueryParam("modelo") modelo: String,
    ): Response {
        val sugestoes = buscarSugestoesImagensUseCase.buscarSugestoes(marca, modelo)
        return Response.ok(sugestoes).build()
    }
}
