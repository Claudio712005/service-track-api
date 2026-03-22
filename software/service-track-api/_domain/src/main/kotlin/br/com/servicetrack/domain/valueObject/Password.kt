package br.com.servicetrack.domain.valueObject

import br.com.servicetrack.domain.exception.DomainException

@JvmInline
value class Password(val value: String) {
    init {
        if(value.length < 6) {
            throw DomainException("Password must be at least 6 characters long")
        }

        if(!value.any { it.isUpperCase() }) {
            throw DomainException("Password must contain at least one uppercase letter")
        }

        if(!value.any { it.isLowerCase() }) {
            throw DomainException("Password must contain at least one lowercase letter")
        }

        if(!value.any { it.isDigit() }) {
            throw DomainException("Password must contain at least one digit")
        }

        if(!value.any { "!@#\$%^&*()".contains(it) }) {
            throw DomainException("Password must contain at least one special character")
        }
    }
}