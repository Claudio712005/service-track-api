package br.com.servicetrack.infrastructure.auditoria.strategy

import br.com.servicetrack.application.auditoria.dto.AuditoriaContextoDTO
import br.com.servicetrack.domain.auditoria.Auditoria
import br.com.servicetrack.domain.auditoria.EventoAuditoria
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.auditoria.vo.DadosAuditoria

class LoginAuditoriaStrategy : AuditoriaStrategy {

    override fun suporta(evento: TipoEventoAuditoria) =
        evento == TipoEventoAuditoria.LOGIN

    override fun executar(ctx: AuditoriaContextoDTO): Auditoria {
        val dados = DadosAuditoria.evento()
        return Auditoria.registrar(
            referenciaId = ctx.referenciaId,
            eventoAuditoria = EventoAuditoria.login(ctx.entidade),
            dados = dados,
            enderecoIp = ctx.enderecoIp,
            responsavelAcao = ctx.responsavelAcao,
        )
    }
}
