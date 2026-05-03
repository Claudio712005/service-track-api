package br.com.servicetrack.application.insumo.dto

import br.com.servicetrack.domain.insumo.Insumo
import java.math.BigDecimal

data class InsumoResDTO(
    val id: String,
    val nome: String,
    val descricao: String,
    val custo: BigDecimal,
    val estoqueMinimo: Int,
    val qtdEstoque: Int
) {
    companion object {
        fun de(insumo: Insumo) = InsumoResDTO(
            id = insumo.id.valor,
            nome = insumo.nome,
            descricao = insumo.descricao,
            custo = insumo.custo.valor,
            estoqueMinimo = insumo.estoqueMinimo,
            qtdEstoque = insumo.obterQtdEstoque()
        )
    }
}
