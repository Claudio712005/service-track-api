package br.com.servicetrack.application.auditoria.dto

import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.auditoria.vo.EnderecoIp
import br.com.servicetrack.domain.auditoria.vo.ReferenciaId
import br.com.servicetrack.domain.usuario.vo.UsuarioId

data class AuditoriaContextoDTO(
    val entidade: TipoEntidade,
    val evento: TipoEventoAuditoria,
    val antes: Any?,
    val depois: Any?,
    val referenciaId: ReferenciaId,
    val enderecoIp: EnderecoIp,
    val responsavelAcao: UsuarioId,
)
