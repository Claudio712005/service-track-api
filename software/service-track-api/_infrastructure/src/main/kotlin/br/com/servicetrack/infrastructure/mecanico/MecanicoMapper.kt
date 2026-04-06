package br.com.servicetrack.infrastructure.mecanico

import br.com.servicetrack.application.mecanico.dto.request.CadastrarMecanicoReqDTO
import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO
import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import br.com.servicetrack.infrastructure.api.dto.CadastrarMecanicoRequest
import br.com.servicetrack.infrastructure.api.dto.MecanicoResponse
import java.util.UUID

internal fun CadastrarMecanicoRequest.toApplicationDTO() = CadastrarMecanicoReqDTO(
    nome = nome,
    email = email,
    senha = senha,
    telefone = telefone,
    cpf = cpf,
    dataNascimento = dataNascimento,
    nivel = NivelMecanicoEnum.valueOf(nivel.name),
    valorHora = valorHora.toBigDecimal()
)

internal fun MecanicoResDTO.toMecanicoResponse(): MecanicoResponse = MecanicoResponse()
    .usuarioId(UUID.fromString(usuarioId))
    .nome(nome)
    .email(email)
    .cpf(cpf)
    .telefone(telefone)
    .nivel(nivel)
    .valorHora(valorHora.toDouble())
    .roles(roles.map { it.name })
    .ativo(ativo)
