package br.com.servicetrack.domain.valueObject

import br.com.servicetrack.domain.exception.DomainException

@JvmInline
value class PhoneNumber(val value: String) {
    init {
        if(value.length < 8) {
            throw DomainException("Phone number cannot be blank")
        }
    }
}