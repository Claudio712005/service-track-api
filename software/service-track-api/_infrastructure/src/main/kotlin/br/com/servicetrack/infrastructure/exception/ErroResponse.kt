package br.com.servicetrack.infrastructure.exception

data class ErroResponse(
    val mensagem: String,
    val detalhe: String? = null
)
