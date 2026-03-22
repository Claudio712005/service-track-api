package br.com.servicetrack.domain.valueObject

import br.com.servicetrack.domain.exception.DomainException

@JvmInline
value class Email(val value: String) {
    init {
        if (value.isBlank()) {
            throw DomainException("Email cannot be blank")
        }

        if (!Regex("^[A-Za-z0-9+_.-]+@(.+)$").matches(value)) {
            throw DomainException("Invalid email format")
        }
    }
}