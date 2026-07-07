package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class Email(val valor: String) {

    init {
        if (valor.isBlank()) {
            throw DomainException("E-mail não pode ser vazio")
        }

        if (!FORMATO_EMAIL.matches(valor)) {
            throw DomainException("E-mail inválido")
        }
    }

    companion object {
        private val FORMATO_EMAIL =
            Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
    }
}
