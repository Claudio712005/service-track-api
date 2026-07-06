package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.dto.request.ReprovarOrcamentoReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.ReprovarOrcamentoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.service.support.DecididorOrcamento
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId

class ReprovarOrcamentoService(
    private val osRepository: OrdemServicoRepositoryPort,
    private val jwt: JwtPort,
    private val decididor: DecididorOrcamento,
) : ReprovarOrcamentoUseCase {

    @Auditavel(entidade = TipoEntidade.ORCAMENTO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun reprovarOrcamento(ordemServicoId: String, req: ReprovarOrcamentoReqDTO): ResumoOrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val os = osRepository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        if (os.clienteId != solicitanteId) {
            throw OperacaoNegadaException(
                "reprovação de orçamento",
                "Apenas o cliente titular da OS pode reprovar o orçamento",
            )
        }

        return ResumoOrdemServicoResDTO.de(decididor.reprovar(os, req.motivo))
    }
}
