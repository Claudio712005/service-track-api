package br.com.servicetrack.infrastructure.ordemServico.persistence

import br.com.servicetrack.domain.orcamento.Orcamento
import br.com.servicetrack.domain.orcamento.vo.OrcamentoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orcamentos")
class OrcamentoEntity : PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    lateinit var id: UUID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false, updatable = false, unique = true)
    lateinit var ordemServico: OrdemServicoEntity

    @Column(name = "data_criacao", nullable = false, updatable = false)
    lateinit var dataCriacao: LocalDateTime

    @Column(name = "data_atualizacao", nullable = false)
    lateinit var dataAtualizacao: LocalDateTime

    @Column(name = "custo_mao_de_obra", precision = 12, scale = 2, nullable = false)
    lateinit var custoMaoDeObra: BigDecimal

    @Column(name = "custo_insumos", precision = 12, scale = 2, nullable = false)
    lateinit var custoInsumos: BigDecimal

    @Column(name = "aprovado", nullable = false)
    var aprovado: Boolean = false

    @Column(name = "observacao", columnDefinition = "TEXT")
    var observacao: String = ""

    companion object : PanacheCompanion<OrcamentoEntity> {

        fun de(orcamento: Orcamento, ordemServicoRef: OrdemServicoEntity): OrcamentoEntity = OrcamentoEntity().apply {
            id = UUID.fromString(orcamento.id.valor)
            ordemServico = ordemServicoRef
            dataCriacao = orcamento.dataCriacao
            dataAtualizacao = orcamento.obterDataAtualizacao()
            custoMaoDeObra = orcamento.custoMaoDeObra.valor
            custoInsumos = orcamento.custoInsumos.valor
            aprovado = orcamento.estaAprovado()
            observacao = orcamento.obterObservacao()
        }
    }

    fun atualizarCom(orcamento: Orcamento) {
        this.dataAtualizacao = orcamento.obterDataAtualizacao()
        this.custoMaoDeObra = orcamento.custoMaoDeObra.valor
        this.custoInsumos = orcamento.custoInsumos.valor
        this.aprovado = orcamento.estaAprovado()
        this.observacao = orcamento.obterObservacao()
    }

    fun toDomain(): Orcamento = Orcamento.reconstituir(
        id = OrcamentoId.de(id.toString()),
        dataCriacao = dataCriacao,
        dataAtualizacao = dataAtualizacao,
        custoMaoDeObra = ValorMonetario(custoMaoDeObra),
        custoInsumos = ValorMonetario(custoInsumos),
        aprovado = aprovado,
        observacao = observacao,
    )
}
