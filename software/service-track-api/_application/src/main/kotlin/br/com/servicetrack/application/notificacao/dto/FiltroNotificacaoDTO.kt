package br.com.servicetrack.application.notificacao.dto

data class FiltroNotificacaoDTO(
    val destinatarioId: String,
    val visualizada: Boolean? = null,
    val page: Int = 0,
    val size: Int = 20,
)
