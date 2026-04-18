package br.com.servicetrack.application.auditoria.ports.out

import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria

interface RegistrarAuditoriaPort {
    fun registrar(
        entidade: TipoEntidade,
        evento: TipoEventoAuditoria,
        referenciaId: String,
        antes: Any?,
        depois: Any?,
    )
}
