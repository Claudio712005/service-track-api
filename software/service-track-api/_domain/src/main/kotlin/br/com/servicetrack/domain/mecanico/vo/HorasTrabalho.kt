package br.com.servicetrack.domain.mecanico.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class HorasTrabalho(val valor: Int) {

    init {
        if (valor <= 0) {
            throw DomainException("Horas de trabalho devem ser maior que zero")
        }
    }
}
