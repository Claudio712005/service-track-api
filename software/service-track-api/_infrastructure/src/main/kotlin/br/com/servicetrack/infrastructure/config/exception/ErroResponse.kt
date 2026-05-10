package br.com.servicetrack.infrastructure.config.exception

data class ErroResponse(
    val mensagem: String,
    val detalhe: String? = null
)
