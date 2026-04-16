package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.insumo.dto.InsumoResDTO
import br.com.servicetrack.application.insumo.dto.InsumoResumoResDTO
import br.com.servicetrack.application.insumo.ports.`in`.ListarInsumosUseCase
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort

class ListarInsumosService(
    private val repository: InsumoRepositoryPort
) : ListarInsumosUseCase {

    override fun listarInsumos(): List<InsumoResDTO> =
        repository.listarTodos().map { InsumoResDTO.de(it) }

    override fun listarResumidos(): List<InsumoResumoResDTO> =
        repository.listarTodos().map { InsumoResumoResDTO.de(it) }
}
