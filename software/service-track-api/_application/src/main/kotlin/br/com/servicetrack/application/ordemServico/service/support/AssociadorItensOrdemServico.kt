package br.com.servicetrack.application.ordemServico.service.support

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.application.ordemServico.dto.request.ItemInsumoReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.ItemServicoReqDTO
import br.com.servicetrack.application.servico.ports.`out`.ServicoRepositoryPort
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario

class AssociadorItensOrdemServico(
    private val servicoRepository: ServicoRepositoryPort,
    private val insumoRepository: InsumoRepositoryPort,
) {

    fun associar(
        os: OrdemServico,
        servicos: List<ItemServicoReqDTO>,
        insumos: List<ItemInsumoReqDTO>,
    ) {
        if (servicos.isEmpty() || insumos.isEmpty()) {
            throw DomainException("São necessários pelo menos 1 serviço e 1 insumo para associar à OS")
        }

        os.listarServicos().forEach { os.removerServico(it.servicoId) }
        os.listarInsumos().toSet().forEach { os.removerInsumo(it) }

        servicos.forEach { itemReq ->
            val servico = servicoRepository.buscarPorId(ServicoId(itemReq.servicoId))
                ?: throw EntidadeNaoEncontradaException("Serviço", arrayOf(itemReq.servicoId))

            val valor = itemReq.valorCobrado
                ?.let { ValorMonetario(it) }
                ?: servico.valorReferencia
                ?: throw DomainException(
                    "Serviço '${servico.nomeServico}' não possui valor de referência; informe o valorCobrado",
                )

            os.adicionarServico(servico.id, valor)
        }

        insumos.forEach { itemReq ->
            require(itemReq.quantidade > 0) { "Quantidade do insumo deve ser maior que zero" }

            val insumo = insumoRepository.buscarPorId(InsumoId.de(itemReq.insumoId))
                ?: throw EntidadeNaoEncontradaException("Insumo", arrayOf(itemReq.insumoId))

            if (insumo.obterQtdEstoque() < itemReq.quantidade) {
                throw DomainException(
                    "Estoque insuficiente para o insumo '${insumo.nome}': " +
                        "disponível ${insumo.obterQtdEstoque()}, solicitado ${itemReq.quantidade}",
                )
            }

            repeat(itemReq.quantidade) { os.adicionarInsumo(insumo.id) }
        }
    }
}
