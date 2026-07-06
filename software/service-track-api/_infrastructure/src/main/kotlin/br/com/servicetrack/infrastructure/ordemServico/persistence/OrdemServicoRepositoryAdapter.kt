package br.com.servicetrack.infrastructure.ordemServico.persistence

import br.com.servicetrack.application.dashboard.dto.query.OrdemServicoDashboardQueryDTO
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
import java.math.BigDecimal
import java.util.UUID

@ApplicationScoped
class OrdemServicoRepositoryAdapter : OrdemServicoRepositoryPort {

    @Inject
    lateinit var entityManager: EntityManager

    private companion object {
        const val ORDER_BY_PRIORIDADE_STATUS =
            " order by case status" +
                " when br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum.EM_EXECUCAO then 0" +
                " when br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum.AGUARDANDO_APROVACAO then 1" +
                " when br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum.EM_DIAGNOSTICO then 2" +
                " when br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum.RECEBIDA then 3" +
                " else 4 end asc, dataCriacao asc"
    }

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

        val statusFiltro = filtro.status
        if (statusFiltro != null) {
            conditions.add("status = ?$paramIndex")
            params.add(statusFiltro)
            paramIndex++
        } else {
            conditions.add("status not in ?$paramIndex")
            params.add(listOf(StatusOrdemServicoEnum.FINALIZADA, StatusOrdemServicoEnum.ENTREGUE))
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

        val where = conditions.joinToString(" and ")
        val query = OrdemServicoEntity.find(where + ORDER_BY_PRIORIDADE_STATUS, *params.toTypedArray())
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

    override fun listarAtivasDashboardPorCliente(
        clienteId: UsuarioId,
        limit: Int,
    ): List<OrdemServicoDashboardQueryDTO> {
        val activeStatuses = listOf(
            StatusOrdemServicoEnum.RECEBIDA,
            StatusOrdemServicoEnum.EM_DIAGNOSTICO,
            StatusOrdemServicoEnum.AGUARDANDO_APROVACAO,
            StatusOrdemServicoEnum.EM_EXECUCAO,
            StatusOrdemServicoEnum.ENTREGUE,
        )
        val entities = entityManager.createQuery(
            """
            SELECT os FROM OrdemServicoEntity os
            LEFT JOIN FETCH os.veiculo
            LEFT JOIN FETCH os.mecanico
            LEFT JOIN FETCH os.orcamento
            WHERE os.cliente.id = ?1 AND os.status IN ?2
            ORDER BY os.dataCriacao DESC
            """,
            OrdemServicoEntity::class.java
        )
            .setParameter(1, UUID.fromString(clienteId.valor))
            .setParameter(2, activeStatuses)
            .setMaxResults(limit)
            .resultList

        return entities.map { it.toOrdemServicoDashboardQueryDTO() }
    }

    override fun listarRecentesDashboardPorCliente(
        clienteId: UsuarioId,
        limit: Int,
    ): List<OrdemServicoDashboardQueryDTO> {
        val entities = entityManager.createQuery(
            """
            SELECT os FROM OrdemServicoEntity os
            LEFT JOIN FETCH os.veiculo
            LEFT JOIN FETCH os.mecanico
            LEFT JOIN FETCH os.orcamento
            WHERE os.cliente.id = ?1
            ORDER BY os.dataCriacao DESC
            """,
            OrdemServicoEntity::class.java
        )
            .setParameter(1, UUID.fromString(clienteId.valor))
            .setMaxResults(limit)
            .resultList

        return entities.map { it.toOrdemServicoDashboardQueryDTO() }
    }

    override fun contarPorClienteEStatus(
        clienteId: UsuarioId,
        statuses: List<StatusOrdemServicoEnum>,
    ): Long = OrdemServicoEntity.count(
        "cliente.id = ?1 and status in ?2",
        UUID.fromString(clienteId.valor),
        statuses,
    )

    override fun contarTotalPorVeiculo(veiculoId: VeiculoId, clienteId: UsuarioId): Long =
        OrdemServicoEntity.count(
            "veiculo.id = ?1 and cliente.id = ?2",
            UUID.fromString(veiculoId.valor),
            UUID.fromString(clienteId.valor),
        )

    override fun somarGastoPorVeiculo(veiculoId: VeiculoId, clienteId: UsuarioId): BigDecimal =
        entityManager.createQuery(
            """
            SELECT COALESCE(SUM(o.custoMaoDeObra + o.custoInsumos), 0)
            FROM OrcamentoEntity o
            WHERE o.ordemServico.veiculo.id = ?1
              AND o.ordemServico.cliente.id = ?2
              AND o.aprovado = true
            """,
            BigDecimal::class.java
        )
            .setParameter(1, UUID.fromString(veiculoId.valor))
            .setParameter(2, UUID.fromString(clienteId.valor))
            .singleResult ?: BigDecimal.ZERO

    private fun OrdemServicoEntity.toOrdemServicoDashboardQueryDTO() = OrdemServicoDashboardQueryDTO(
        id = id.toString(),
        motivo = motivo,
        status = status,
        veiculoId = veiculo.id.toString(),
        veiculoPlaca = veiculo.placa,
        veiculoModelo = veiculo.modelo,
        mecanicoId = mecanico.id.toString(),
        mecanicoNome = mecanico.nome,
        dataCriacao = dataCriacao,
        dataAtualizacao = dataAtualizacao,
        prazoConclusao = prazoConclusao,
        valorOrcado = orcamento?.let { it.custoMaoDeObra.add(it.custoInsumos) },
    )
}
