package br.com.servicetrack.domain.mecanico.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import java.math.BigDecimal

@JvmInline
value class ValorHora(val valor: BigDecimal) {

    init {
        if (valor <= BigDecimal.ZERO) {
            throw DomainException("Valor da hora deve ser maior que zero")
        }
    }
}