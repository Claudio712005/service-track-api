package br.com.servicetrack.application.ordemServico.dto.request

import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import java.math.BigDecimal

data class AssociarItensReqDTO(
    val servicos: List<ItemServicoReqDTO>,
    val insumos: List<ItemInsumoReqDTO>,
)

data class ItemServicoReqDTO(
    @field:Rastreavel
    val servicoId: String,
    val valorCobrado: BigDecimal?,
)

data class ItemInsumoReqDTO(
    @field:Rastreavel
    val insumoId: String,
    val quantidade: Int,
)
