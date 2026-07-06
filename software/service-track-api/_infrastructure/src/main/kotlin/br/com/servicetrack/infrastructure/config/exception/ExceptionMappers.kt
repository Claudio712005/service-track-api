package br.com.servicetrack.infrastructure.config.exception

import br.com.servicetrack.application.exception.CredenciaisInvalidasException
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.IntegracaoExternaException
import br.com.servicetrack.application.exception.MarcaInvalidaFipeException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.exception.UsuarioJaExisteException
import br.com.servicetrack.application.exception.VeiculoJaExisteException
import br.com.servicetrack.domain.shared.exception.DomainException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.smallrye.faulttolerance.api.RateLimitException
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import org.jboss.logging.Logger

@Provider
class UsuarioJaExisteExceptionMapper : ExceptionMapper<UsuarioJaExisteException> {
    override fun toResponse(exception: UsuarioJaExisteException): Response =
        Response.status(Response.Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Conflito de cadastro", detalhe = exception.message))
            .build()
}

@Provider
class VeiculoJaExisteExceptionMapper : ExceptionMapper<VeiculoJaExisteException> {
    override fun toResponse(exception: VeiculoJaExisteException): Response =
        Response.status(Response.Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Conflito de cadastro", detalhe = exception.message))
            .build()
}

@Provider
class CredenciaisInvalidasExceptionMapper : ExceptionMapper<CredenciaisInvalidasException> {
    override fun toResponse(exception: CredenciaisInvalidasException): Response =
        Response.status(Response.Status.UNAUTHORIZED)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Não autorizado", detalhe = exception.message))
            .build()
}

@Provider
class OperacaoNegadaExceptionMapper : ExceptionMapper<OperacaoNegadaException> {
    override fun toResponse(exception: OperacaoNegadaException): Response =
        Response.status(Response.Status.FORBIDDEN)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Operação não permitida", detalhe = exception.message))
            .build()
}

@Provider
class EntidadeNaoEncontradaExceptionMapper : ExceptionMapper<EntidadeNaoEncontradaException> {
    override fun toResponse(exception: EntidadeNaoEncontradaException): Response =
        Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Recurso não encontrado", detalhe = exception.message))
            .build()
}

@Provider
class IllegalStateExceptionMapper : ExceptionMapper<IllegalStateException> {
    override fun toResponse(exception: IllegalStateException): Response =
        Response.status(Response.Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Operação não permitida no estado atual", detalhe = exception.message))
            .build()
}

@Provider
class DomainExceptionMapper : ExceptionMapper<DomainException> {
    override fun toResponse(exception: DomainException): Response =
        Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Dados inválidos", detalhe = exception.message))
            .build()
}

@Provider
class MarcaInvalidaFipeExceptionMapper : ExceptionMapper<MarcaInvalidaFipeException> {
    override fun toResponse(exception: MarcaInvalidaFipeException): Response =
        Response.status(422)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Marca inválida", detalhe = exception.message))
            .build()
}

@Provider
class IntegracaoExternaExceptionMapper : ExceptionMapper<IntegracaoExternaException> {
    override fun toResponse(exception: IntegracaoExternaException): Response =
        Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Serviço externo indisponível", detalhe = exception.message))
            .build()
}

@Provider
class ConstraintViolationExceptionMapper : ExceptionMapper<ConstraintViolationException> {
    override fun toResponse(exception: ConstraintViolationException): Response {
        val detalhe = exception.constraintViolations
            .joinToString("; ") { "${it.propertyPath}: ${it.message}" }
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Dados de entrada inválidos", detalhe = detalhe))
            .build()
    }
}

@Provider
class JsonProcessingExceptionMapper : ExceptionMapper<JsonProcessingException> {
    private val log = Logger.getLogger(JsonProcessingExceptionMapper::class.java)

    override fun toResponse(exception: JsonProcessingException): Response {
        val detalhe = when (exception) {
            is InvalidFormatException -> {
                val esperado = exception.targetType?.simpleName ?: "tipo esperado"
                "Campo '${campoDe(exception)}' com valor inválido: '${exception.value}' (esperado $esperado)"
            }
            is MismatchedInputException -> "Campo '${campoDe(exception)}' ausente ou com tipo inválido"
            else -> "Corpo da requisição não é um JSON válido"
        }
        log.warnf("Falha ao desserializar corpo da requisição: %s", detalhe)
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Dados inválidos", detalhe = detalhe))
            .build()
    }

    private fun campoDe(exception: MismatchedInputException): String =
        exception.path.joinToString(".") { it.fieldName ?: "[${it.index}]" }.ifBlank { "(raiz)" }
}

@Provider
class TimeoutExceptionMapper : ExceptionMapper<TimeoutException> {
    private val log = Logger.getLogger(TimeoutExceptionMapper::class.java)

    override fun toResponse(exception: TimeoutException): Response {
        log.warnf("Timeout na camada de recurso: %s", exception.message)
        return Response.status(Response.Status.GATEWAY_TIMEOUT)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Tempo limite excedido", detalhe = "A requisição excedeu o tempo máximo permitido"))
            .build()
    }
}

@Provider
class BulkheadExceptionMapper : ExceptionMapper<BulkheadException> {
    private val log = Logger.getLogger(BulkheadExceptionMapper::class.java)

    override fun toResponse(exception: BulkheadException): Response {
        log.warnf("Bulkhead atingido: %s", exception.message)
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Serviço sobrecarregado", detalhe = "Limite de requisições simultâneas atingido. Tente novamente em instantes"))
            .build()
    }
}

@Provider
class RateLimitExceptionMapper : ExceptionMapper<RateLimitException> {
    private val log = Logger.getLogger(RateLimitExceptionMapper::class.java)

    override fun toResponse(exception: RateLimitException): Response {
        log.warnf("Rate limit excedido: %s", exception.message)
        return Response.status(429)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Limite de requisições excedido", detalhe = "Muitas requisições em curto período. Aguarde antes de tentar novamente"))
            .build()
    }
}

@Provider
class CircuitBreakerOpenExceptionMapper : ExceptionMapper<CircuitBreakerOpenException> {
    private val log = Logger.getLogger(CircuitBreakerOpenExceptionMapper::class.java)

    override fun toResponse(exception: CircuitBreakerOpenException): Response {
        log.errorf("Circuit breaker aberto: %s", exception.message)
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Serviço temporariamente indisponível", detalhe = "Circuito de proteção ativado após falhas consecutivas. Tente novamente em 1 minuto"))
            .build()
    }
}
