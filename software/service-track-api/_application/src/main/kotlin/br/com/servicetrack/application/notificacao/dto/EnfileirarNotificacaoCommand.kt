package br.com.servicetrack.application.notificacao.dto

import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.usuario.vo.UsuarioId

data class EnfileirarNotificacaoCommand(
    val assunto: AssuntoNotificacao,
    val titulo: TituloNotificacao,
    val descricao: DescricaoNotificacao,
    val variaveis: VariaveisTemplate,
    val tipoNotificacao: TipoNotificacao,
    val tipoConteudoNotificacao: TipoConteudoNotificacao,
    val destinatario: UsuarioId,
    val copias: List<UsuarioId> = emptyList(),
)

