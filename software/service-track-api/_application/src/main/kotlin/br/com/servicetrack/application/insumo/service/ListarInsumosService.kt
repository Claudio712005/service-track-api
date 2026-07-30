package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.insumo.dto.InsumoResDTO
import br.com.servicetrack.application.insumo.dto.InsumoResumoResDTO
import br.com.servicetrack.application.insumo.ports.`in`.ListarInsumosUseCase
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.application.observabilidade.annotation.Observavel
import br.com.servicetrack.application.observabilidade.enums.CodigoUseCase

class ListarInsumosService(
    private val repository: InsumoRepositoryPort
) : ListarInsumosUseCase {

    @Observavel(codigo = CodigoUseCase.INSUMO_LISTAR)

    override fun listarInsumos(): List<InsumoResDTO> =
        repository.listarTodos().map { InsumoResDTO.de(it) }

    @Observavel(codigo = CodigoUseCase.INSUMO_CATALOGO)

    override fun listarResumidos(): List<InsumoResumoResDTO> =
        repository.listarTodos().map { InsumoResumoResDTO.de(it) }
}
