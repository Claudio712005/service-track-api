package br.com.servicetrack.domain.usuario.vo

import java.util.UUID

@JvmInline
value class UsuarioId(val valor: String) {
    companion object {
        fun gerar() = UsuarioId(UUID.randomUUID().toString())
    }
}