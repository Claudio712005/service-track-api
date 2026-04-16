package br.com.servicetrack.infrastructure.servico.persistence

import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class ServicoRepositoryAdapter : ServicoRepositoryPort {

    override fun salvar(servico: Servico) {
        ServicoEntity.de(servico).persist()
    }

    override fun buscarPorId(id: ServicoId): Servico? {
        return ServicoEntity
            .find("id", UUID.fromString(id.valor))
            .firstResult()
            ?.toDomain()
    }

    override fun listarTodos(): List<Servico> {
        return ServicoEntity.listAll().map { it.toDomain() }
    }

    override fun atualizar(servico: Servico) {
        val entity = ServicoEntity
            .find("id", UUID.fromString(servico.id.valor))
            .firstResult() ?: return

        entity.nomeServico = servico.nomeServico
        entity.descricaoServico = servico.descricaoServico
        entity.valorReferencia = servico.valorReferencia?.valor
    }

    override fun remover(id: ServicoId) {
        ServicoEntity.delete("id", UUID.fromString(id.valor))
    }
}
