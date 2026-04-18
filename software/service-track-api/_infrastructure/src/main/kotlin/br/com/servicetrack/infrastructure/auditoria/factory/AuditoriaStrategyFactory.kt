package br.com.servicetrack.infrastructure.auditoria.factory

import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.infrastructure.auditoria.strategy.AuditoriaStrategy

class AuditoriaStrategyFactory(
    private val strategies: List<AuditoriaStrategy>,
) {
    fun obter(evento: TipoEventoAuditoria): AuditoriaStrategy =
        strategies.firstOrNull { it.suporta(evento) }
            ?: throw IllegalArgumentException("Nenhuma estratégia de auditoria encontrada para o evento: $evento")
}
