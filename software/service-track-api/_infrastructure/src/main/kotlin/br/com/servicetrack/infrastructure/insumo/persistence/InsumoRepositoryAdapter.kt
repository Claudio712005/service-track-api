package br.com.servicetrack.infrastructure.insumo.persistence

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
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

    override fun buscarPorId(id: InsumoId): Insumo? =
        InsumoEntity
            .find("id = ?1 and ativo = ?2", UUID.fromString(id.valor), true)
            .firstResult()
            ?.toDomain()

    override fun listarTodos(): List<Insumo> =
        InsumoEntity.list("ativo", true).map { it.toDomain() }

    override fun atualizar(insumo: Insumo) {
        val entity = InsumoEntity
            .find("id = ?1 and ativo = ?2", UUID.fromString(insumo.id.valor), true)
            .firstResult() ?: return

        entity.nome = insumo.nome
        entity.descricao = insumo.descricao
        entity.custo = insumo.custo.valor
        entity.estoqueMinimo = insumo.estoqueMinimo
        entity.qtdEstoque = insumo.obterQtdEstoque()
    }

    override fun desativar(id: InsumoId) {
        val entity = InsumoEntity
            .find("id = ?1 and ativo = ?2", UUID.fromString(id.valor), true)
            .firstResult()
            ?: throw EntidadeNaoEncontradaException(Insumo::class.java.name, arrayOf(id.valor))

        entity.ativo = false
    }
}
