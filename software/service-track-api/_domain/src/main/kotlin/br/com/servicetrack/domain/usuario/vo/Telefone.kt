package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class Telefone(val valor: String) {
    init {
        if (valor.isBlank()) {
            throw DomainException("Telefone não pode ser vazio")
        }

        if (!valor.all { it.isDigit() }) {
            throw DomainException("Telefone deve conter apenas números")
        }

        if (valor.length < 10 || valor.length > 11) {
            throw DomainException("Telefone deve conter entre 10 e 11 dígitos")
        }
    }
}
