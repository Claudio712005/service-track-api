package br.com.servicetrack.infrastructure.auditoria.strategy

import br.com.servicetrack.application.auditoria.dto.AuditoriaContextoDTO
import br.com.servicetrack.domain.auditoria.Auditoria
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria

interface AuditoriaStrategy {
    fun suporta(evento: TipoEventoAuditoria): Boolean

    fun executar(contexto: AuditoriaContextoDTO): Auditoria
}