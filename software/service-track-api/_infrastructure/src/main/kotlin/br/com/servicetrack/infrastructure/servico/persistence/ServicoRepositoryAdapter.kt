package br.com.servicetrack.infrastructure.servico.persistence

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
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

    override fun buscarPorId(id: ServicoId): Servico? =
        ServicoEntity
            .find("id = ?1 and ativo = ?2", UUID.fromString(id.valor), true)
            .firstResult()
            ?.toDomain()

    override fun listarTodos(): List<Servico> =
        ServicoEntity.list("ativo", true).map { it.toDomain() }

    override fun atualizar(servico: Servico) {
        val entity = ServicoEntity
            .find("id = ?1 and ativo = ?2", UUID.fromString(servico.id.valor), true)
            .firstResult() ?: return

        entity.nomeServico = servico.nomeServico
        entity.descricaoServico = servico.descricaoServico
        entity.valorReferencia = servico.valorReferencia?.valor
    }

    override fun desativar(id: ServicoId) {
        val entity = ServicoEntity
            .find("id = ?1 and ativo = ?2", UUID.fromString(id.valor), true)
            .firstResult()
            ?: throw EntidadeNaoEncontradaException(Servico::class.java.name, arrayOf(id.valor))

        entity.ativo = false
    }
}
