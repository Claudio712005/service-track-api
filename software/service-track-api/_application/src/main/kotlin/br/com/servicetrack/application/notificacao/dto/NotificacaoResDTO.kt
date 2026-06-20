package br.com.servicetrack.application.notificacao.dto

import br.com.servicetrack.domain.notificacao.Notificacao
import java.time.LocalDateTime

data class NotificacaoResDTO(
    val id: String,
    val titulo: String,
    val assunto: String,
    val descricao: String,
    val tipoNotificacao: String,
    val tipoConteudo: String,
    val statusEnvio: String,
    val visualizada: Boolean,
    val dataCriacao: LocalDateTime,
    val dataEnvio: LocalDateTime?,
    val dataVisualizacao: LocalDateTime?,
) {
    companion object {
        fun de(notificacao: Notificacao): NotificacaoResDTO = NotificacaoResDTO(
            id = notificacao.id.value,
            titulo = notificacao.titulo.valor,
            assunto = notificacao.assunto.valor,
            descricao = notificacao.descricao.valor,
            tipoNotificacao = notificacao.tipoNotificacao.name,
            tipoConteudo = notificacao.tipoConteudoNotificacao.name,
            statusEnvio = notificacao.statusEnvio.name,
            visualizada = notificacao.visualizada,
            dataCriacao = notificacao.dataCriacao,
            dataEnvio = notificacao.dataEnvio,
            dataVisualizacao = notificacao.dataVisualizacao,
        )
    }
}
