package br.com.servicetrack.infrastructure.auditoria.strategy

import br.com.servicetrack.application.auditoria.dto.AuditoriaContextoDTO
import br.com.servicetrack.domain.auditoria.Auditoria
import br.com.servicetrack.domain.auditoria.EventoAuditoria
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.auditoria.vo.DadosAuditoria

class RemocaoAuditoriaStrategy : AuditoriaStrategy {

    override fun suporta(evento: TipoEventoAuditoria) =
        evento == TipoEventoAuditoria.REMOVIDO

    override fun executar(ctx: AuditoriaContextoDTO): Auditoria {
        val dados = if (ctx.antes != null) {
            DadosAuditoria.remocao(antes = ctx.antes!!)
        } else {
            DadosAuditoria.remocaoSemEstado()
        }
        return Auditoria.registrar(
            referenciaId = ctx.referenciaId,
            eventoAuditoria = EventoAuditoria.removido(ctx.entidade),
            dados = dados,
            enderecoIp = ctx.enderecoIp,
            responsavelAcao = ctx.responsavelAcao,
        )
    }
}
