package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class Telefone(val valor: String) {
    init {
        if(valor.length < 8) {
            throw DomainException("Telefone não pode ser vazio e deve conter ao menos 8 dígitos")
        }
    }
}