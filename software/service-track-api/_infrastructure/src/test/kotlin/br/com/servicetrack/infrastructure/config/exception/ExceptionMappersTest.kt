package br.com.servicetrack.infrastructure.config.exception

import io.smallrye.faulttolerance.api.RateLimitException
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExceptionMappersTest {

    @Test
    fun `TimeoutException deve retornar 504 Gateway Timeout`() {
        val mapper = TimeoutExceptionMapper()
        val response = mapper.toResponse(TimeoutException())

        assertEquals(504, response.status)
        val body = response.entity as ErroResponse
        assertEquals("Tempo limite excedido", body.mensagem)
    }

    @Test
    fun `BulkheadException deve retornar 503 Service Unavailable`() {
        val mapper = BulkheadExceptionMapper()
        val response = mapper.toResponse(BulkheadException("bulkhead full"))

        assertEquals(503, response.status)
        val body = response.entity as ErroResponse
        assertEquals("Serviço sobrecarregado", body.mensagem)
    }

    @Test
    fun `RateLimitException deve retornar 429 Too Many Requests`() {
        val mapper = RateLimitExceptionMapper()
        val response = mapper.toResponse(RateLimitException("rate limit exceeded"))

        assertEquals(429, response.status)
        val body = response.entity as ErroResponse
        assertEquals("Limite de requisições excedido", body.mensagem)
    }

    @Test
    fun `CircuitBreakerOpenException deve retornar 503 Service Unavailable`() {
        val mapper = CircuitBreakerOpenExceptionMapper()
        val response = mapper.toResponse(CircuitBreakerOpenException("circuit open"))

        assertEquals(503, response.status)
        val body = response.entity as ErroResponse
        assertEquals("Serviço temporariamente indisponível", body.mensagem)
    }
}
