package br.com.servicetrack.infrastructure.mecanico.persistence

import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.domain.mecanico.Mecanico
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MecanicoRepositoryAdapter : MecanicoRepositoryPort {

    override fun salvar(mecanico: Mecanico) {
        MecanicoEntity.de(mecanico).persist()
    }

    override fun buscarPorId(usuarioId: String): Mecanico? {
        val entity = MecanicoEntity.find("usuarioId", usuarioId).firstResult() ?: return null
        return entity.toDomain()
    }
}
