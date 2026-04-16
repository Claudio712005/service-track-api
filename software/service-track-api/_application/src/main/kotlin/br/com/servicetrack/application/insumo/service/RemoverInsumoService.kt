package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.insumo.ports.`in`.RemoverInsumoUseCase
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId

class RemoverInsumoService(
    private val repository: InsumoRepositoryPort
) : RemoverInsumoUseCase {

    override fun removerInsumo(id: InsumoId) {
        repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Insumo::class.java.name, arrayOf(id.valor))
        repository.remover(id)
    }
}
