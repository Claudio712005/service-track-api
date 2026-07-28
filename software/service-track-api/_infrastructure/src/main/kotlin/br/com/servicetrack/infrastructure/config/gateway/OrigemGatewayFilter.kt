package br.com.servicetrack.infrastructure.config.gateway

import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.PreMatching
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.util.Optional

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 100)
class OrigemGatewayFilter(
    @ConfigProperty(name = "servicetrack.gateway.segredo")
    private val segredoConfigurado: Optional<String>,
) : ContainerRequestFilter {

    private val segredoEsperado: String = segredoConfigurado.orElse("")

    private companion object {
        const val HEADER = "x-origem-gateway"
        val CAMINHOS_ISENTOS = listOf("/q/health", "/q/metrics", "/q/openapi", "/q/swagger-ui")
    }

    private val log = Logger.getLogger(OrigemGatewayFilter::class.java)

    override fun filter(requisicao: ContainerRequestContext) {
        if (segredoEsperado.isBlank()) return

        val caminho = "/" + requisicao.uriInfo.path.trimStart('/')
        if (CAMINHOS_ISENTOS.any { caminho.startsWith(it) }) return

        if (requisicao.getHeaderString(HEADER) != segredoEsperado) {
            log.warnf("Requisicao recusada fora do gateway: %s %s", requisicao.method, caminho)
            requisicao.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                    .entity(mapOf("mensagem" to "Acesso permitido apenas pelo API Gateway."))
                    .build()
            )
        }
    }
}
