package br.com.servicetrack.infrastructure.auditoria.factory

import br.com.servicetrack.application.auditoria.dto.AuditoriaContextoDTO
import br.com.servicetrack.domain.auditoria.Auditoria
import br.com.servicetrack.domain.auditoria.EventoAuditoria
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.auditoria.vo.DadosAuditoria
import br.com.servicetrack.infrastructure.auditoria.strategy.AuditoriaStrategy

class CriacaoAuditoriaStrategy : AuditoriaStrategy {

    override fun suporta(evento: TipoEventoAuditoria) =
        evento == TipoEventoAuditoria.CRIADO

    override fun executar(ctx: AuditoriaContextoDTO): Auditoria {

        val dados = DadosAuditoria.criacao(
            depois = ctx.depois!!
        )

        return Auditoria.registrar(
            referenciaId = ctx.referenciaId,
            eventoAuditoria = EventoAuditoria.criacao(ctx.entidade),
            dados = dados,
            enderecoIp = ctx.enderecoIp,
            responsavelAcao = ctx.responsavelAcao,
        )
    }
}