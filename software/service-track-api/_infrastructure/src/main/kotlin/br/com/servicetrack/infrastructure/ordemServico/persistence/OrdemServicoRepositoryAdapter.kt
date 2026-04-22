package br.com.servicetrack.infrastructure.ordemServico.persistence

import br.com.servicetrack.application.ordemServico.dto.request.FiltroOrdemServicoDTO
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.shared.dto.PageResDTO
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import br.com.servicetrack.infrastructure.usuario.persistence.UsuarioEntity
import io.quarkus.panache.common.Page
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import java.util.UUID

@ApplicationScoped
class OrdemServicoRepositoryAdapter : OrdemServicoRepositoryPort {

    @Inject
    lateinit var entityManager: EntityManager

    private fun buscarEntidade(id: UUID): OrdemServicoEntity? =
        OrdemServicoEntity.find("id", id).firstResult()

    override fun salvar(ordemServico: OrdemServico): OrdemServico {
        val entity = OrdemServicoEntity.de(ordemServico)
        entity.persist()
        return entity.toDomain()
    }

    override fun atualizar(ordemServico: OrdemServico): OrdemServico {
        val entity = buscarEntidade(UUID.fromString(ordemServico.id.valor))
            ?: return salvar(ordemServico)

        entity.observacao = ordemServico.observacao
        entity.dataAtualizacao = ordemServico.dataAtualizacao
        entity.status = ordemServico.obterStatus()

        val mecanicoUuid = UUID.fromString(ordemServico.obterMecanicoId().valor)
        entity.mecanico = entityManager.getReference(UsuarioEntity::class.java, mecanicoUuid)

        val domainOrcamento = ordemServico.obterOrcamento()
        when {
            domainOrcamento == null -> entity.orcamento = null
            entity.orcamento == null -> entity.orcamento = OrcamentoEntity.de(domainOrcamento, entity)
            else -> entity.orcamento!!.atualizarCom(domainOrcamento)
        }

        entity.insumos = ordemServico.listarInsumos().map { it.valor }.toMutableList()

        val dominioItens = ordemServico.listarServicos()
        val dominioIds = dominioItens.mapTo(mutableSetOf()) { UUID.fromString(it.id.valor) }
        val existentesPorId = entity.itensServico.associateBy { it.id }

        entity.itensServico.removeIf { it.id !in dominioIds }

        for (itemDomain in dominioItens) {
            val uuid = UUID.fromString(itemDomain.id.valor)
            val existente = existentesPorId[uuid]
            if (existente != null) {
                existente.atualizarCom(itemDomain, entityManager)
            } else {
                entity.itensServico.add(ItemOrdemServicoEntity.de(itemDomain, entity))
            }
        }

        entity.persistAndFlush()
        return entity.toDomain()
    }

    override fun buscarPorId(id: OrdemServicoId): OrdemServico? =
        buscarEntidade(UUID.fromString(id.valor))?.toDomain()

    override fun listar(filtro: FiltroOrdemServicoDTO): PageResDTO<OrdemServico> {
        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()
        var paramIndex = 1

        filtro.status?.let {
            conditions.add("status = ?$paramIndex")
            params.add(it)
            paramIndex++
        }
        if (filtro.clienteId != null) {
            conditions.add("cliente.id = ?$paramIndex")
            params.add(UUID.fromString(filtro.clienteId))
            paramIndex++
        }
        if (filtro.mecanicoId != null) {
            conditions.add("mecanico.id = ?$paramIndex")
            params.add(UUID.fromString(filtro.mecanicoId))
            paramIndex++
        }

        val where = if (conditions.isEmpty()) "1=1" else conditions.joinToString(" and ")
        val query = OrdemServicoEntity.find(where, *params.toTypedArray())
            .page(Page.of(filtro.page, filtro.size))

        val content = query.list().map { it.toDomain() }
        val total = query.count()

        return PageResDTO.de(content, filtro.page, filtro.size, total)
    }

    override fun contarOsAbertaPorIdVeiculoEIdCliente(
        clienteId: UsuarioId,
        veiculoId: VeiculoId,
    ): Long {
        val statusFinalizados = listOf(
            StatusOrdemServicoEnum.CANCELADA.name,
            StatusOrdemServicoEnum.ENTREGUE.name,
        )
        return OrdemServicoEntity.count(
            "cliente.id = ?1 and veiculo.id = ?2 and status not in ?3",
            UUID.fromString(clienteId.valor),
            UUID.fromString(veiculoId.valor),
            statusFinalizados,
        )
    }
}
