package br.com.servicetrack.application.servico.dto

import java.math.BigDecimal

data class CriarServicoReqDTO(
    val nomeServico: String,
    val descricaoServico: String,
    val valorReferencia: BigDecimal? = null
)
