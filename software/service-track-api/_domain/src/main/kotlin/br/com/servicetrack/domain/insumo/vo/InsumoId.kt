package br.com.servicetrack.domain.insumo.vo

import java.util.UUID

@JvmInline
value class InsumoId private constructor(val value: String) {

    companion object {
        fun gerar() = InsumoId(UUID.randomUUID().toString())
    }
}