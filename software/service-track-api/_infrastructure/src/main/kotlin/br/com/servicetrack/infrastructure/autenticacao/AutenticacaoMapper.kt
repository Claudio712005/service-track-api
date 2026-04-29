package br.com.servicetrack.infrastructure.autenticacao

import br.com.servicetrack.application.usuario.dto.request.LoginReqDTO
import br.com.servicetrack.application.usuario.dto.request.ResetarSenhaReqDTO
import br.com.servicetrack.application.usuario.dto.response.LoginResDTO
import br.com.servicetrack.infrastructure.api.dto.LoginRequest
import br.com.servicetrack.infrastructure.api.dto.LoginResponse
import br.com.servicetrack.infrastructure.api.dto.ResetarSenhaRequest
import java.util.UUID

internal fun LoginRequest.toApplicationDTO() = LoginReqDTO(
    email = email,
    senha = senha
)

internal fun ResetarSenhaRequest.toApplicationDTO() = ResetarSenhaReqDTO(
    senhaAtual = senhaAtual,
    novaSenha = novaSenha,
    confirmacaoNovaSenha = confirmacaoNovaSenha
)

internal fun LoginResDTO.toLoginResponse(): LoginResponse = LoginResponse()
    .token(token)
    .usuarioId(UUID.fromString(usuarioId))
    .nome(nome)
    .email(email)
    .roles(roles.map { it.name })
