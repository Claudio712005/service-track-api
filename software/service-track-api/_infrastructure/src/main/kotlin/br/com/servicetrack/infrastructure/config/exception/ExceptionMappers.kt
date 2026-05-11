package br.com.servicetrack.infrastructure.config.exception

import br.com.servicetrack.application.exception.CredenciaisInvalidasException
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.IntegracaoExternaException
import br.com.servicetrack.application.exception.MarcaInvalidaFipeException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.exception.UsuarioJaExisteException
import br.com.servicetrack.application.exception.VeiculoJaExisteException
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
