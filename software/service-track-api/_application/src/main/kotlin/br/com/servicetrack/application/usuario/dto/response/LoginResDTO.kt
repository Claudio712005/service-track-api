package br.com.servicetrack.application.usuario.dto.response

import br.com.servicetrack.domain.shared.enums.Role

data class LoginResDTO(
    val token: String,
    val usuarioId: String,
    val nome: String,
    val email: String,
    val roles: Set<Role>
)
