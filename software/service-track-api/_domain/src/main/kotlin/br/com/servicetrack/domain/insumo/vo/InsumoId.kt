package br.com.servicetrack.domain.insumo.vo

import java.util.UUID

@JvmInline
value class InsumoId private constructor(val valor: String) {
    companion object {
        fun gerar() = InsumoId(UUID.randomUUID().toString())
        fun de(valor: String) = InsumoId(valor)
    }
}
