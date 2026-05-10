package br.com.servicetrack.application.mecanico.mapper

import br.com.servicetrack.application.mecanico.dto.request.CadastrarMecanicoReqDTO
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone

internal fun CadastrarMecanicoReqDTO.toDomain(senhaHash: String): Usuario = Usuario.criarMecanico(
    nome = nome,
    email = Email(email),
    senha = Senha.deHash(senhaHash),
    dataNascimento = dataNascimento,
    telefone = Telefone(telefone),
    cpf = Cpf(cpf)
)
