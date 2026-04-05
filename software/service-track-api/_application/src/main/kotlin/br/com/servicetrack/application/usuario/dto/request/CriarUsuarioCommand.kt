package br.com.servicetrack.application.usuario.dto.request

import br.com.servicetrack.domain.shared.enums.Role
import java.time.LocalDate

data class CriarUsuarioCommand(
    val nome: String,
    val email: String,
    val senha: String,
    val telefone: String,
    val cpf: String,
    val dataNascimento: LocalDate,
    val roles: Set<Role>
)