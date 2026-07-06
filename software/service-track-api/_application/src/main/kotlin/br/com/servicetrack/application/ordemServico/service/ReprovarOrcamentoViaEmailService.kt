package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.ReprovarOrcamentoViaEmailUseCase
import br.com.servicetrack.application.ordemServico.service.support.DecididorOrcamento
import br.com.servicetrack.application.ordemServico.service.support.ResolvedorOrdemServicoPorToken
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria

class ReprovarOrcamentoViaEmailService(
    private val resolvedor: ResolvedorOrdemServicoPorToken,
    private val decididor: DecididorOrcamento,
) : ReprovarOrcamentoViaEmailUseCase {

    private companion object {
        const val MOTIVO_PADRAO = "Reprovado pelo cliente via e-mail"
    }

    @Auditavel(entidade = TipoEntidade.ORCAMENTO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun reprovar(token: String): ResumoOrdemServicoResDTO {
        val os = resolvedor.resolver(token)
        return ResumoOrdemServicoResDTO.de(decididor.reprovar(os, MOTIVO_PADRAO))
    }
}
