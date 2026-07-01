package br.com.servicetrack.application.notificacao.dto

data class ConteudoRenderizado(
    val assunto: String,
    val corpoHtml: String,
    val corpoTexto: String,
)

