package br.com.servicetrack.application.notificacao.dto

import br.com.servicetrack.domain.usuario.vo.Email

data class EmailMensagem(
    val destinatario: Email,
    val copias: List<Email>,
    val assunto: String,
    val corpoHtml: String,
    val corpoTexto: String,
)

