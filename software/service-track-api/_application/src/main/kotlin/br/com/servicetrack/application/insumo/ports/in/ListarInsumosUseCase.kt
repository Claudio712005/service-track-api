package br.com.servicetrack.application.insumo.ports.`in`

import br.com.servicetrack.application.insumo.dto.InsumoResDTO
import br.com.servicetrack.application.insumo.dto.InsumoResumoResDTO

interface ListarInsumosUseCase {
    fun listarInsumos(): List<InsumoResDTO>
    fun listarResumidos(): List<InsumoResumoResDTO>
}
