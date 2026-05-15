package br.com.servicetrack.domain.notificacao

import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import java.time.LocalDateTime

data class Notificacao(
    val id: NotificacaoId,
    val titulo: String,
    val descricao: String,
    val valores: Map<String, String>,
    val tipoNotificacao: TipoNotificacao,
    val tipoConteudoNotificacao: TipoConteudoNotificacao,
    var visualizada: Boolean,
    var dataVisualizacao: LocalDateTime?,
    val dataEnvio: LocalDateTime,
    val destinatario: UsuarioId
){
    companion object {
        fun gerar(
            titulo: String,
            descricao: String,
            valores: Map<String, String>,
            tipoNotificacao: TipoNotificacao,
            tipoConteudoNotificacao: TipoConteudoNotificacao,
            destinatario: UsuarioId
        ) = Notificacao(
            NotificacaoId.gerar(),
            titulo,
            descricao,
            valores,
            tipoNotificacao,
            tipoConteudoNotificacao,
            visualizada = false,
            null,
            LocalDateTime.now(),
            destinatario
        )
    }

    fun visualizar(){
        visualizada = true
        dataVisualizacao = LocalDateTime.now()
    }
}