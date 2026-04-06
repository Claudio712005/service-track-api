package br.com.servicetrack.infrastructure.usuario

import br.com.servicetrack.application.usuario.dto.request.CadastrarClienteReqDTO
import br.com.servicetrack.application.usuario.dto.response.ClienteResDTO
import br.com.servicetrack.infrastructure.api.dto.CadastrarClienteRequest
import br.com.servicetrack.infrastructure.api.dto.ClienteResponse
import java.util.UUID

internal fun CadastrarClienteRequest.toApplicationDTO() = CadastrarClienteReqDTO(
    nome = nome,
    email = email,
    senha = senha,
    telefone = telefone,
    cpf = cpf,
    dataNascimento = dataNascimento
)

internal fun ClienteResDTO.toClienteResponse(): ClienteResponse = ClienteResponse()
    .id(UUID.fromString(id))
    .nome(nome)
    .email(email)
    .cpf(cpf)
    .telefone(telefone)
    .roles(roles.map { it.name })
    .ativo(ativo)
