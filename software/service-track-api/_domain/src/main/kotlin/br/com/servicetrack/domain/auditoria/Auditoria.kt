package br.com.servicetrack.domain.auditoria

import br.com.servicetrack.domain.auditoria.vo.AuditoriaId
import br.com.servicetrack.domain.auditoria.vo.DadosAuditoria
import br.com.servicetrack.domain.auditoria.vo.EnderecoIp
import br.com.servicetrack.domain.auditoria.vo.ReferenciaId
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import java.time.LocalDateTime

class Auditoria(
    private val id: AuditoriaId,
    private val enderecoIp: EnderecoIp,
    private val referenciaId: ReferenciaId,
    private val dataCriacao: LocalDateTime,
    private val eventoAuditoria: EventoAuditoria,
    private val dados: DadosAuditoria,
    private val responsavelAcao: UsuarioId,
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