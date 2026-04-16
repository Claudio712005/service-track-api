package br.com.servicetrack.infrastructure.insumo.persistence

import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class InsumoRepositoryAdapter : InsumoRepositoryPort {

    override fun salvar(insumo: Insumo) {
        InsumoEntity.de(insumo).persist()
    }

    override fun buscarPorId(id: InsumoId): Insumo? {
        return InsumoEntity
            .find("id", UUID.fromString(id.valor))
            .firstResult()
            ?.toDomain()
    }

    override fun listarTodos(): List<Insumo> {
        return InsumoEntity.listAll().map { it.toDomain() }
    }

    override fun atualizar(insumo: Insumo) {
        val entity = InsumoEntity
            .find("id", UUID.fromString(insumo.id.valor))
            .firstResult() ?: return

        entity.nome = insumo.nome
        entity.descricao = insumo.descricao
        entity.custo = insumo.custo.valor
        entity.estoqueMinimo = insumo.estoqueMinimo
        entity.qtdEstoque = insumo.obterQtdEstoque()
    }

    override fun remover(id: InsumoId) {
        InsumoEntity.delete("id", UUID.fromString(id.valor))
    }
}
