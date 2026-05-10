package br.com.servicetrack.infrastructure.mecanico.persistence

import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.domain.mecanico.Mecanico
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class MecanicoRepositoryAdapter : MecanicoRepositoryPort {

    override fun salvar(mecanico: Mecanico) {
        MecanicoEntity.de(mecanico).persist()
    }

    override fun buscarPorId(usuarioId: String): Mecanico? {
        val entity = MecanicoEntity.find("usuarioId", UUID.fromString(usuarioId)).firstResult() ?: return null
        return entity.toDomain()
    }

    override fun listarTodos(): List<Mecanico> {
        return MecanicoEntity.listAll().map { it.toDomain() }
    }

    override fun atualizar(mecanico: Mecanico): Mecanico? {
        val entity = MecanicoEntity
            .find("usuarioId", UUID.fromString(mecanico.usuarioId.valor))
            .firstResult() ?: return null

        entity.nivel = mecanico.obterNivel().valor
        entity.valorHora = mecanico.obterValorHora().valor

        return entity.toDomain()
    }
}
