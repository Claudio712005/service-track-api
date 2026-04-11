package br.com.servicetrack.infrastructure.ordemServico.persistence

import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.PrazoConclusao
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import br.com.servicetrack.infrastructure.usuario.persistence.UsuarioEntity
import br.com.servicetrack.infrastructure.veiculo.persistence.VeiculoEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "ordens_servico")
class OrdemServicoEntity : PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    lateinit var id: UUID

    @Column(name = "motivo", nullable = false, length = 500)
    lateinit var motivo: String

    @Column(name = "observacao", columnDefinition = "TEXT")
    var observacao: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, updatable = false)
    lateinit var cliente: UsuarioEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mecanico_id", nullable = false)
    lateinit var mecanico: UsuarioEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false, updatable = false)
    lateinit var veiculo: VeiculoEntity

    @Column(name = "data_criacao", nullable = false, updatable = false)
    lateinit var dataCriacao: LocalDateTime

    @Column(name = "data_atualizacao", nullable = false)
    lateinit var dataAtualizacao: LocalDateTime

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var status: StatusOrdemServicoEnum

    @Column(name = "prazo_conclusao")
    var prazoConclusao: LocalDateTime? = null

    @Embedded
    var orcamento: OrcamentoEmbeddable? = null

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "ordem_servico_insumos",
        joinColumns = [JoinColumn(name = "ordem_servico_id")]
    )
    @Column(name = "insumo_id", nullable = false)
    var insumos: MutableList<String> = mutableListOf()

    @OneToMany(
        mappedBy = "ordemServico",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var itensServico: MutableList<ItemOrdemServicoEntity> = mutableListOf()

    companion object : PanacheCompanion<OrdemServicoEntity> {

        fun de(os: OrdemServico): OrdemServicoEntity = OrdemServicoEntity().apply {
            id = UUID.fromString(os.id.valor)
            motivo = os.motivo
            dataCriacao = os.dataCriacao

            cliente = UsuarioEntity().apply {
                this.id = UUID.fromString(os.clienteId.valor)
            }
            mecanico = UsuarioEntity().apply {
                this.id = UUID.fromString(os.obterMecanicoId().valor)
            }
            veiculo = VeiculoEntity().apply {
                this.id = UUID.fromString(os.veiculoId.valor)
            }

            status = os.obterStatus()
            orcamento = os.obterOrcamento()?.let { OrcamentoEmbeddable.de(it) }
            insumos = os.listarInsumos().map { it.valor }.toMutableList()
        }
    }

    fun toDomain(): OrdemServico = OrdemServico.reconstituir(
        id = OrdemServicoId(id.toString()),
        motivo = motivo,
        observacao = observacao,
        clienteId = UsuarioId(cliente.id.toString()),
        mecanicoId = UsuarioId(mecanico.id.toString()),
        veiculoId = VeiculoId(veiculo.id.toString()),
        dataCriacao = dataCriacao,
        dataAtualizacao = dataAtualizacao,
        status = StatusOrdemServico.deEnum(status),
        prazoConclusao = prazoConclusao?.let { PrazoConclusao(it) },
        orcamento = orcamento?.toDomainOrNull(),
        insumos = insumos.map { InsumoId.de(it) }.toMutableList(),
        itensServico = itensServico.map { it.toDomain() }.toMutableList(),
    )
}
