package br.com.servicetrack.infrastructure.exception

import br.com.servicetrack.application.exception.UsuarioJaExisteException
import br.com.servicetrack.domain.shared.exception.DomainException
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class UsuarioJaExisteExceptionMapper : ExceptionMapper<UsuarioJaExisteException> {
    override fun toResponse(exception: UsuarioJaExisteException): Response =
        Response.status(Response.Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(ErroResponse(mensagem = "Conflito de cadastro", detalhe = exception.message))
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
