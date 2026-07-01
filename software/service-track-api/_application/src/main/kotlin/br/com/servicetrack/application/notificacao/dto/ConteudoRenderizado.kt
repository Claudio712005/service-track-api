package br.com.servicetrack.application.notificacao.dto

/**
 * Conteúdo de uma notificação renderizado a partir de um template e variáveis.
 * Produzido pelo `TemplateRendererPort`, consumido pelo `EmailGatewayPort`.
 */
data class ConteudoRenderizado(
    val assunto: String,
    val corpoHtml: String,
    val corpoTexto: String,
)

