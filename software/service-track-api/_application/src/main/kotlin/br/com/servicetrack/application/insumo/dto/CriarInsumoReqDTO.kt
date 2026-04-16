package br.com.servicetrack.application.insumo.dto

import java.math.BigDecimal

data class CriarInsumoReqDTO(
    val nome: String,
    val descricao: String,
    val custo: BigDecimal,
    val qtdEstoqueInicial: Int = 0,
    val estoqueMinimo: Int = 0
)
