package br.com.servicetrack.domain.auditoria

import br.com.servicetrack.domain.auditoria.enums.TipoDadoAuditoria

class CampoAlterado<T>(
    val campo: String,
    val valorAntes: T?,
    val valorDepois: T?,
    val tipo: TipoDadoAuditoria
)
