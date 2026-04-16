package br.com.servicetrack.application.insumo.dto

import br.com.servicetrack.domain.insumo.Insumo

data class InsumoResumoResDTO(
    val id: String,
    val nome: String,
    val descricao: String
) {
    companion object {
        fun de(insumo: Insumo) = InsumoResumoResDTO(
            id = insumo.id.valor,
            nome = insumo.nome,
            descricao = insumo.descricao
        )
    }
}
