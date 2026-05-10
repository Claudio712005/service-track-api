package br.com.servicetrack.application.mecanico.dto.request

import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import java.math.BigDecimal

data class AtualizarMecanicoReqDTO(
    val nivel: NivelMecanicoEnum,
    val valorHora: BigDecimal
)
