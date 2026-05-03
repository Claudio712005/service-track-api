package br.com.servicetrack.domain.auditoria

import br.com.servicetrack.domain.auditoria.vo.AuditoriaId
import br.com.servicetrack.domain.auditoria.vo.DadosAuditoria
import br.com.servicetrack.domain.auditoria.vo.EnderecoIp
import br.com.servicetrack.domain.auditoria.vo.ReferenciaId
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import java.time.LocalDateTime

class Auditoria(
    val id: AuditoriaId,
    val enderecoIp: EnderecoIp,
    val referenciaId: ReferenciaId,
    val dataCriacao: LocalDateTime,
    val eventoAuditoria: EventoAuditoria,
    val dados: DadosAuditoria,
    val responsavelAcao: UsuarioId,
) {

    init {
        if(!dados.temAlteracoes()) throw DomainException("Auditoria deve conter alterações")
    }

    companion object {
        fun registrar(
            enderecoIp: EnderecoIp,
            referenciaId: ReferenciaId,
            eventoAuditoria: EventoAuditoria,
            dados: DadosAuditoria,
            responsavelAcao: UsuarioId,
        ) = Auditoria(
            AuditoriaId.gerar(),
            enderecoIp,
            referenciaId,
            LocalDateTime.now(),
            eventoAuditoria,
            dados,
            responsavelAcao
        )
    }
}