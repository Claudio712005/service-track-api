package br.com.servicetrack.application.insumo.ports.`in`

import br.com.servicetrack.application.insumo.dto.InsumoResDTO
import br.com.servicetrack.domain.insumo.vo.InsumoId

interface BuscarInsumoUseCase {
    fun buscarInsumo(id: InsumoId): InsumoResDTO
}
