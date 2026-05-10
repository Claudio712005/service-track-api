package br.com.servicetrack.domain.auditoria.vo

@JvmInline
value class AuditoriaId(val value: String) {

    companion object {
        fun gerar(): AuditoriaId {
            return AuditoriaId(java.util.UUID.randomUUID().toString())
        }
    }
}