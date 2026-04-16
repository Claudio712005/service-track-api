package br.com.servicetrack.application.insumo.ports.out

import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId

interface InsumoRepositoryPort {

    fun salvar(insumo: Insumo)
    fun buscarPorId(id: InsumoId): Insumo?
    fun listarTodos(): List<Insumo>
    fun atualizar(insumo: Insumo)
    fun remover(id: InsumoId)
}
