package br.com.servicetrack.infrastructure.config.gateway

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Optional

class OrigemGatewayFilterTest {

    private val segredo = "segredo-do-gateway"

    private fun requisicao(caminho: String, header: String?): ContainerRequestContext {
        val uriInfo = mockk<UriInfo>()
        every { uriInfo.path } returns caminho

        val contexto = mockk<ContainerRequestContext>(relaxed = true)
        every { contexto.uriInfo } returns uriInfo
        every { contexto.method } returns "GET"
        every { contexto.getHeaderString("x-origem-gateway") } returns header
        return contexto
    }

    private fun statusRecusado(contexto: ContainerRequestContext): Int? {
        val resposta = slot<Response>()
        return runCatching {
            verify { contexto.abortWith(capture(resposta)) }
            resposta.captured.status
        }.getOrNull()
    }

    @Test
    fun `aceita requisicao com o segredo do gateway`() {
        val contexto = requisicao("ordem-servico/lista", segredo)

        OrigemGatewayFilter(Optional.of(segredo)).filter(contexto)

        verify(exactly = 0) { contexto.abortWith(any()) }
    }

    @Test
    fun `recusa requisicao sem o header`() {
        val contexto = requisicao("ordem-servico/lista", null)

        OrigemGatewayFilter(Optional.of(segredo)).filter(contexto)

        assertEquals(403, statusRecusado(contexto))
    }

    @Test
    fun `recusa requisicao com segredo errado`() {
        val contexto = requisicao("clientes", "tentativa")

        OrigemGatewayFilter(Optional.of(segredo)).filter(contexto)

        assertEquals(403, statusRecusado(contexto))
    }

    @Test
    fun `isenta os probes do kubernetes`() {
        listOf("q/health/live", "q/health/ready", "q/health/started").forEach { caminho ->
            val contexto = requisicao(caminho, null)

            OrigemGatewayFilter(Optional.of(segredo)).filter(contexto)

            verify(exactly = 0) { contexto.abortWith(any()) }
        }
    }

    @Test
    fun `desligado quando o segredo nao esta configurado`() {
        val contexto = requisicao("ordem-servico/lista", null)

        OrigemGatewayFilter(Optional.empty()).filter(contexto)

        verify(exactly = 0) { contexto.abortWith(any()) }
    }
}
