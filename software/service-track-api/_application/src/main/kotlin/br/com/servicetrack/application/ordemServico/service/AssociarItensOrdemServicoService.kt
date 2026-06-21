package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.application.ordemServico.dto.request.AssociarItensReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.AssociarItensOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.servico.ports.`out`.ServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario

class AssociarItensOrdemServicoService(
    private val osRepository: OrdemServicoRepositoryPort,
    private val servicoRepository: ServicoRepositoryPort,
    private val insumoRepository: InsumoRepositoryPort,
    private val jwt: JwtPort,
) : AssociarItensOrdemServicoUseCase {

    @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun associarItens(ordemServicoId: String, req: AssociarItensReqDTO): OrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val os = osRepository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        if (os.obterMecanicoId() != solicitanteId) {
            throw OperacaoNegadaException(
                "associação de itens",
                "Apenas o mecânico vinculado à OS pode associar serviços e insumos"
            )
        }

        if (os.obterStatus() != StatusOrdemServicoEnum.EM_DIAGNOSTICO) {
            throw OperacaoNegadaException(
                "associação de itens",
                "Serviços e insumos só podem ser associados quando a OS está em diagnóstico"
            )
        }

        if (req.servicos.isEmpty() || req.insumos.isEmpty()) {
            throw DomainException("São necessários pelo menos 1 serviço e 1 insumo para associar à OS")
        }

        os.listarServicos().forEach { os.removerServico(it.servicoId) }
        os.listarInsumos().toSet().forEach { os.removerInsumo(it) }

        req.servicos.forEach { itemReq ->
            val servico = servicoRepository.buscarPorId(ServicoId(itemReq.servicoId))
                ?: throw EntidadeNaoEncontradaException("Serviço", arrayOf(itemReq.servicoId))

            val valor = itemReq.valorCobrado
                ?.let { ValorMonetario(it) }
                ?: servico.valorReferencia
                ?: throw DomainException("Serviço '${servico.nomeServico}' não possui valor de referência; informe o valorCobrado")

            os.adicionarServico(servico.id, valor)
        }

        req.insumos.forEach { itemReq ->
            require(itemReq.quantidade > 0) { "Quantidade do insumo deve ser maior que zero" }

            val insumo = insumoRepository.buscarPorId(InsumoId.de(itemReq.insumoId))
                ?: throw EntidadeNaoEncontradaException("Insumo", arrayOf(itemReq.insumoId))

            if (insumo.obterQtdEstoque() < itemReq.quantidade) {
                throw DomainException(
                    "Estoque insuficiente para o insumo '${insumo.nome}': " +
                        "disponível ${insumo.obterQtdEstoque()}, solicitado ${itemReq.quantidade}"
                )
            }

            repeat(itemReq.quantidade) { os.adicionarInsumo(insumo.id) }
        }

        return OrdemServicoResDTO.de(osRepository.atualizar(os))
    }
}
