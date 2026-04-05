package br.com.servicetrack.application.usuario.mapper

import br.com.servicetrack.application.usuario.dto.request.CriarUsuarioCommand
import br.com.servicetrack.application.usuario.dto.response.UsuarioResponse
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone

internal fun CriarUsuarioCommand.toDomain(senhaHash: String): Usuario = Usuario.criar(
    nome = nome,
    email = Email(email),
    senha = Senha.deHash(senhaHash),
    dataNascimento = dataNascimento,
    telefone = Telefone(telefone),
    cpf = Cpf(cpf),
    roles = roles
)