package br.com.servicetrack.application.notificacao.ports.out

import br.com.servicetrack.domain.usuario.vo.Telefone

interface NotificacaoServicePort {

    fun enviarNotificacaoEmail(
        conteudo: Map<String, String>,
        titulo: String,
        destinatario: String,
        copias: String,
        assunto: String
    ): Boolean

}