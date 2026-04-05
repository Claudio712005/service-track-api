package br.com.servicetrack.infrastructure.usuario

import br.com.servicetrack.application.usuario.dto.request.CriarUsuarioCommand
import br.com.servicetrack.application.usuario.dto.response.UsuarioResponse
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.infrastructure.api.dto.CadastrarClienteRequest
import br.com.servicetrack.infrastructure.api.dto.ClienteResponse
import java.util.UUID

internal fun CadastrarClienteRequest.toCommand() = CriarUsuarioCommand(
    nome = nome,
    email = email,
    senha = senha,
    telefone = telefone,
    cpf = cpf,
    dataNascimento = dataNascimento,
    roles = setOf(Role.CLIENTE)
)

internal fun UsuarioResponse.toClienteResponse(): ClienteResponse = ClienteResponse()
    .id(UUID.fromString(id))
    .nome(nome)
    .email(email)
    .cpf(cpf)
    .telefone(telefone)
    .roles(roles.map { it.name })
    .ativo(ativo)
