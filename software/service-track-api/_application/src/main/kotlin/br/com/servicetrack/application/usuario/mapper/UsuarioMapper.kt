package br.com.servicetrack.application.usuario.mapper

import br.com.servicetrack.application.usuario.dto.request.CadastrarClienteReqDTO
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone

internal fun CadastrarClienteReqDTO.toDomain(senhaHash: String): Usuario = Usuario.criarCliente(
    nome = nome,
    email = Email(email),
    senha = Senha.deHash(senhaHash),
    dataNascimento = dataNascimento,
    telefone = Telefone(telefone),
    cpf = Cpf(cpf)
)
