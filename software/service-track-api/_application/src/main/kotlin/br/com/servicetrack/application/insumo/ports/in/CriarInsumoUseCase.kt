package br.com.servicetrack.application.insumo.ports.`in`

import br.com.servicetrack.application.insumo.dto.CriarInsumoReqDTO
import br.com.servicetrack.application.insumo.dto.InsumoResDTO

interface CriarInsumoUseCase {
    fun criarInsumo(req: CriarInsumoReqDTO): InsumoResDTO
}
