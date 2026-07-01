package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.ordemServico.dto.request.CancelarOsReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.CancelarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import jakarta.enterprise.event.Event

class CancelarOrdemServicoService(
    private val osRepository: OrdemServicoRepositoryPort,
    private val insumoRepository: InsumoRepositoryPort,
    private val jwt: JwtPort,
    private val statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
) : CancelarOrdemServicoUseCase {

    private val statusPermitidosParaCancelamento = setOf(
        StatusOrdemServicoEnum.RECEBIDA,
        StatusOrdemServicoEnum.AGUARDANDO_APROVACAO,
    )

    @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun cancelarOrdemServico(ordemServicoId: String, req: CancelarOsReqDTO): ResumoOrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val os = osRepository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        if (os.clienteId != solicitanteId) {
            throw OperacaoNegadaException(
                "cancelamento de OS",
                "Apenas o cliente titular da OS pode cancelá-la"
            )
        }

        if (os.obterStatus() !in statusPermitidosParaCancelamento) {
            throw OperacaoNegadaException(
                "cancelamento de OS",
                "A OS não pode ser cancelada no status atual (${os.obterStatus().descricao}). " +
                    "Cancelamento permitido apenas nos status: Recebida e Aguardando Aprovação"
            )
        }

        if (os.obterStatus() == StatusOrdemServicoEnum.AGUARDANDO_APROVACAO) {
            os.listarInsumos()
                .groupBy { it }
                .forEach { (insumoId, ocorrencias) ->
                    val insumo = insumoRepository.buscarPorId(insumoId)
                        ?: throw EntidadeNaoEncontradaException("Insumo", arrayOf(insumoId.valor))
                    insumo.adicionarAoEstoque(ocorrencias.size)
                    insumoRepository.atualizar(insumo)
                }
        }

        os.cancelar(req.motivo ?: "")

        val atualizada = osRepository.atualizar(os)

        statusAlteradoEvent.fire(
            OrdemServicoStatusAlteradoEvent(
                ordemServicoId = atualizada.id,
                clienteId = atualizada.clienteId,
                novoStatus = atualizada.obterStatus(),
            )
        )

        return ResumoOrdemServicoResDTO.de(atualizada)
    }
}
