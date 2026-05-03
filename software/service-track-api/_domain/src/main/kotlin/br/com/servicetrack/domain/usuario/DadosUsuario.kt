package br.com.servicetrack.domain.usuario

import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import java.time.LocalDate

data class DadosUsuario(
    val id: UsuarioId,
    val nome: String,
    val email: Email,
    val cpf: Cpf,
    val telefone: Telefone,
    val dataNascimento: LocalDate,
    val roles: Set<Role>,
    val ativo: Boolean
)
