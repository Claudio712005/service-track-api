package br.com.servicetrack.domain.veiculo.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class Placa(val valor: String) {

    init {
        if (!FORMATO_PLACA.matches(valor)) {
            throw DomainException("Placa inválida")
        }
    }

    companion object {
        private val FORMATO_PLACA =
            Regex("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}\$")
    }
}