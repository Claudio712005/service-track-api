package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.dto.request.AssociarItensReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.AssociarItensOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.service.support.AssociadorItensOrdemServico
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId

class AssociarItensOrdemServicoService(
    private val osRepository: OrdemServicoRepositoryPort,
    private val jwt: JwtPort,
    private val associador: AssociadorItensOrdemServico,
) : AssociarItensOrdemServicoUseCase {

    @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun associarItens(ordemServicoId: String, req: AssociarItensReqDTO): OrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val os = osRepository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        if (os.obterMecanicoId() != solicitanteId) {
            throw OperacaoNegadaException(
                "associação de itens",
                "Apenas o mecânico vinculado à OS pode associar serviços e insumos",
            )
        }

        if (os.obterStatus() != StatusOrdemServicoEnum.EM_DIAGNOSTICO) {
            throw OperacaoNegadaException(
                "associação de itens",
                "Serviços e insumos só podem ser associados quando a OS está em diagnóstico",
            )
        }

        associador.associar(os, req.servicos, req.insumos)

        return OrdemServicoResDTO.de(osRepository.atualizar(os))
    }
}
