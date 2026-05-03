package br.com.servicetrack.application.insumo.ports.`in`

import br.com.servicetrack.domain.insumo.vo.InsumoId

interface RemoverInsumoUseCase {
    fun removerInsumo(id: InsumoId)
}
