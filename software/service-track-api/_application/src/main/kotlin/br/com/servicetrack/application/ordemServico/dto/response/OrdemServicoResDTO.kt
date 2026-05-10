package br.com.servicetrack.application.ordemServico.dto.response

import br.com.servicetrack.application.orcamento.dto.res.OrcamentoResDTO
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrdemServicoResDTO(
    val id: String,
    val motivo: String,
    val observacao: String,
    val clienteId: String,
    val mecanicoId: String,
    val veiculoId: String,
    val status: StatusOrdemServicoEnum,
    val dataCriacao: LocalDateTime,
    val dataAtualizacao: LocalDateTime,
    val itensServico: List<ItemOrdemServicoResDTO>,
    val insumos: List<String>,
    val orcamento: OrcamentoDetalhesResDTO?,
) {
    companion object {
        fun de(domain: OrdemServico): OrdemServicoResDTO {
            val orc = domain.obterOrcamento()
            return OrdemServicoResDTO(
                id = domain.id.valor,
                motivo = domain.motivo,
                observacao = domain.observacao,
                clienteId = domain.clienteId.valor,
                mecanicoId = domain.obterMecanicoId().valor,
                veiculoId = domain.veiculoId.valor,
                status = domain.obterStatus(),
                dataCriacao = domain.dataCriacao,
                dataAtualizacao = domain.dataAtualizacao,
                itensServico = domain.listarServicos().map { ItemOrdemServicoResDTO.de(it) },
                insumos = domain.listarInsumos().map { it.valor }.distinct(),
                orcamento = orc?.let {
                    OrcamentoDetalhesResDTO(
                        id = it.id.valor,
                        custoMaoDeObra = it.custoMaoDeObra.valor,
                        custoInsumos = it.custoInsumos.valor,
                        valorTotal = it.valorTotal.valor,
                        aprovado = it.estaAprovado(),
                        observacao = it.obterObservacao(),
                        dataCriacao = it.dataCriacao,
                        dataAtualizacao = it.obterDataAtualizacao(),
                    )
                },
            )
        }
    }
}

data class OrcamentoDetalhesResDTO(
    val id: String,
    val custoMaoDeObra: BigDecimal,
    val custoInsumos: BigDecimal,
    val valorTotal: BigDecimal,
    val aprovado: Boolean,
    val observacao: String,
    val dataCriacao: LocalDateTime,
    val dataAtualizacao: LocalDateTime,
)
