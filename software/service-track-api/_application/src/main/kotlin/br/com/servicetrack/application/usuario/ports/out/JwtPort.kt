package br.com.servicetrack.application.usuario.ports.out

import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface JwtPort {

    fun getUsuarioId(token: String): String?

    fun getToken(): String

    fun getUsuarioId(): UsuarioId
}
