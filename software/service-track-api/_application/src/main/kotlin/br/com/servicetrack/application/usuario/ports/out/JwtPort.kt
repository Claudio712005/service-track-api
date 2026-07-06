package br.com.servicetrack.application.usuario.ports.out

import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface JwtPort {

    fun gerarToken(usuarioId: String, email: String, roles: Set<Role>): String

    fun getUsuarioId(token: String): String?

    fun getToken(): String

    fun getUsuarioId(): UsuarioId
}
