package br.com.servicetrack.domain.valueObject

import br.com.servicetrack.domain.exception.DomainException

@JvmInline
value class Cpf(val value: String) {
    init {
        if(value.isBlank()) {
            throw DomainException("Cpf cannot be blank")
        }

        if (!value.matches(Regex("\\d{11}"))){
            throw DomainException("CPF must be exactly 11 digits long")
        }
    }
}