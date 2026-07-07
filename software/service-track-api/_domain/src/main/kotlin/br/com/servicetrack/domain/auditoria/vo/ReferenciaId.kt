package br.com.servicetrack.domain.auditoria.vo

import java.util.UUID

@JvmInline
value class ReferenciaId(val value: String) {

        companion object {
            fun gerar(): ReferenciaId {
                return ReferenciaId(UUID.randomUUID().toString())
            }
        }
}
