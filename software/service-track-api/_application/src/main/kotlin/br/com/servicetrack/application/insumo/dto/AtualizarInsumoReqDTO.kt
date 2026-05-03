package br.com.servicetrack.application.insumo.dto

import java.math.BigDecimal

data class AtualizarInsumoReqDTO(
    val nome: String?,
    val descricao: String?,
    val custo: BigDecimal?,
    val estoqueMinimo: Int?
)
