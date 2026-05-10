package br.com.servicetrack.infrastructure.ordemServico.persistence

import br.com.servicetrack.application.ordemServico.ports.out.ItemOrdemServicoRepositoryPort
import br.com.servicetrack.domain.ordemServico.ItemOrdemServico
import br.com.servicetrack.domain.servico.vo.ServicoId
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class ItemOrdemServicoRepositoryAdapter : ItemOrdemServicoRepositoryPort {

    override fun buscarItensConcluidos(servicoId: ServicoId): List<ItemOrdemServico> {
        return ItemOrdemServicoEntity
            .find(
                "feito = true and servico.id = ?1 and dataRealizacao is not null",
                UUID.fromString(servicoId.valor)
            )
            .list()
            .map { it.toDomain() }
    }
}
