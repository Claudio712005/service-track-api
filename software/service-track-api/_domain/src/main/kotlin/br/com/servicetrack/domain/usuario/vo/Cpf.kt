package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class Cpf(val valor: String) {

    init {
        val cpfLimpo = valor.filter { it.isDigit() }

        if (cpfLimpo.length != 11) {
            throw DomainException("CPF deve conter 11 dígitos")
        }

        if (cpfLimpo.all { it == cpfLimpo[0] }) {
            throw DomainException("CPF inválido")
        }

        if (!cpfValido(cpfLimpo)) {
            throw DomainException("CPF inválido")
        }
    }

    companion object {

        private fun cpfValido(cpf: String): Boolean {
            fun calcularDigito(base: String, pesoInicial: Int): Int {
                val soma = base.mapIndexed { index, char ->
                    char.digitToInt() * (pesoInicial - index)
                }.sum()

                val resto = soma % 11
                return if (resto < 2) 0 else 11 - resto
            }

            val d1 = calcularDigito(cpf.substring(0, 9), 10)
            val d2 = calcularDigito(cpf.substring(0, 10), 11)

            return cpf[9].digitToInt() == d1 && cpf[10].digitToInt() == d2
        }
    }
}