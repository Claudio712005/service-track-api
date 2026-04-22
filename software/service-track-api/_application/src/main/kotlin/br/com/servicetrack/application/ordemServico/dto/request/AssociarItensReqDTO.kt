package br.com.servicetrack.application.ordemServico.dto.request

import java.math.BigDecimal

data class AssociarItensReqDTO(
    val servicos: List<ItemServicoReqDTO>,
    val insumos: List<ItemInsumoReqDTO>,
)

data class ItemServicoReqDTO(
    val servicoId: String,
    val valorCobrado: BigDecimal?,
)

data class ItemInsumoReqDTO(
    val insumoId: String,
    val quantidade: Int,
)
