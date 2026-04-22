package br.com.servicetrack.infrastructure.ordemServico.persistence

import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class OrdemServicoRepositoryAdapter: OrdemServicoRepositoryPort {

    override fun salvar(ordemServico: OrdemServico): OrdemServico {
        val entity = OrdemServicoEntity.de(ordemServico)

        entity.persist()

        return entity.toDomain()
    }

    override fun contarOsAbertaPorIdVeiculoEIdCliente(
        clienteId: UsuarioId,
        veiculoId: VeiculoId
    ): Long {
        val statusOsFinalizada = arrayOf(
            StatusOrdemServicoEnum.CANCELADA,
            StatusOrdemServicoEnum.ENTREGUE
        )

        return OrdemServicoEntity.count(
            "cliente.id = ?1 and veiculo.id = ?2 and status not in ?3",
            UUID.fromString(clienteId.valor),
            UUID.fromString(veiculoId.valor),
            statusOsFinalizada.map { it.name }
        )
    }
}