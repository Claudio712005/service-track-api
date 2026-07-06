package br.com.servicetrack.domain.notificacao

enum class TipoConteudoNotificacao(
 val titulo: String
) {
 MUDANCA_STATUS_OS("Sua Ordem de serviço acabou de receber uma alteração!"),
 SOLICITACAO_APROVACAO_ORCAMENTO_OS("Seu orçamento está pronto! Aprove ou recuse por aqui."),
 DECISAO_ORCAMENTO_OS("O cliente decidiu sobre o orçamento da OS."),

}
