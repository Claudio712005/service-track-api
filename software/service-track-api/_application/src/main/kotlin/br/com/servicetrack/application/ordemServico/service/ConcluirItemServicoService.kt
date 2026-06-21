package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.dto.request.ConcluirItemServicoReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.ConcluirItemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.ordemServico.vo.ItemOrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId

class ConcluirItemServicoService(
    private val osRepository: OrdemServicoRepositoryPort,
    private val jwt: JwtPort,
) : ConcluirItemServicoUseCase {

    @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun concluirItemServico(
        ordemServicoId: String,
        itemServicoId: String,
        req: ConcluirItemServicoReqDTO,
    ): OrdemServicoResDTO {
        val mecanicoId = jwt.getUsuarioId()

        val os = osRepository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        if (os.obterMecanicoId() != mecanicoId) {
            throw OperacaoNegadaException(
                "conclusão de item",
                "Apenas o mecânico vinculado à OS pode concluir itens de serviço"
            )
        }

        os.concluirItemServico(
            itemId = ItemOrdemServicoId.de(itemServicoId),
            mecanicoId = mecanicoId,
            observacao = req.observacao,
        )

        return OrdemServicoResDTO.de(osRepository.atualizar(os))
    }
}
