package br.com.servicetrack.domain.contrato.vo

import java.util.UUID

@JvmInline
value class ContratoId private constructor(val value: String) {
    companion object {
        fun gerar() = ContratoId(UUID.randomUUID().toString())
    }
}