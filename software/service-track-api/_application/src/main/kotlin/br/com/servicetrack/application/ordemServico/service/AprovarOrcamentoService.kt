package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.AprovarOrcamentoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.service.support.DecididorOrcamento
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId

class AprovarOrcamentoService(
    private val repository: OrdemServicoRepositoryPort,
    private val jwt: JwtPort,
    private val decididor: DecididorOrcamento,
) : AprovarOrcamentoUseCase {

    @Auditavel(entidade = TipoEntidade.ORCAMENTO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun aprovarOrcamento(ordemServicoId: String): ResumoOrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val os = repository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        if (os.clienteId != solicitanteId) {
            throw OperacaoNegadaException(
                "aprovação de orçamento",
                "Apenas o cliente titular da OS pode aprovar o orçamento",
            )
        }

        return ResumoOrdemServicoResDTO.de(decididor.aprovar(os))
    }
}
