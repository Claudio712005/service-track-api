package br.com.servicetrack.application.auditoria.annotation

import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Auditavel(
    val entidade: TipoEntidade,
    val evento: TipoEventoAuditoria,
)
