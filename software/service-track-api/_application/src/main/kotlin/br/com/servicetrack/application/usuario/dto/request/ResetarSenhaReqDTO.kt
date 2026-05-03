package br.com.servicetrack.application.usuario.dto.request

data class ResetarSenhaReqDTO(
    val senhaAtual: String,
    val novaSenha: String,
    val confirmacaoNovaSenha: String
)
