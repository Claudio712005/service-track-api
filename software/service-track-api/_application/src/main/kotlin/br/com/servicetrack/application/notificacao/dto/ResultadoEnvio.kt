package br.com.servicetrack.application.notificacao.dto

sealed class ResultadoEnvio {
    data object Sucesso : ResultadoEnvio()
    data class Falha(val motivo: String) : ResultadoEnvio()
}

