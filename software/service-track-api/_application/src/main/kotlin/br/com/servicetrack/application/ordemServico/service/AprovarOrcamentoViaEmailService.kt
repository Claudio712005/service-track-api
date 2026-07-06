package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.AprovarOrcamentoViaEmailUseCase
import br.com.servicetrack.application.ordemServico.service.support.DecididorOrcamento
import br.com.servicetrack.application.ordemServico.service.support.ResolvedorOrdemServicoPorToken
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria

class AprovarOrcamentoViaEmailService(
    private val resolvedor: ResolvedorOrdemServicoPorToken,
    private val decididor: DecididorOrcamento,
) : AprovarOrcamentoViaEmailUseCase {

    @Auditavel(entidade = TipoEntidade.ORCAMENTO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun aprovar(token: String): ResumoOrdemServicoResDTO {
        val os = resolvedor.resolver(token)
        return ResumoOrdemServicoResDTO.de(decididor.aprovar(os))
    }
}
