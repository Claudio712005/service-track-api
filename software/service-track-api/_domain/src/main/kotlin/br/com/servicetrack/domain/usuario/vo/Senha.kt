package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class Senha(val valor: String) {

    init {
        if (valor.length < 6) {
            throw DomainException("A senha deve ter no mínimo 6 caracteres")
        }

        if (!valor.any { it.isUpperCase() }) {
            throw DomainException("A senha deve conter ao menos uma letra maiúscula")
        }

        if (!valor.any { it.isLowerCase() }) {
            throw DomainException("A senha deve conter ao menos uma letra minúscula")
        }

        if (!valor.any { it.isDigit() }) {
            throw DomainException("A senha deve conter ao menos um número")
        }

        if (!valor.any { "!@#\$%^&*()".contains(it) }) {
            throw DomainException("A senha deve conter ao menos um caractere especial")
        }
    }
}