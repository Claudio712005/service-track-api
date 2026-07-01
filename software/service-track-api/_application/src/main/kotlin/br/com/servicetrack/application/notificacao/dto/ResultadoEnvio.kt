package br.com.servicetrack.application.notificacao.dto

/**
 * Resultado tipado de uma tentativa de envio de e-mail.
 * Evita propagar exceções de infraestrutura para o use case.
 */
sealed class ResultadoEnvio {
    data object Sucesso : ResultadoEnvio()
    data class Falha(val motivo: String) : ResultadoEnvio()
}

