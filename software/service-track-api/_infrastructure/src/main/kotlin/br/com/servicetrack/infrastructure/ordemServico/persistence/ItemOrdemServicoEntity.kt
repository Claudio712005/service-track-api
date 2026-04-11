package br.com.servicetrack.infrastructure.ordemServico.persistence

import br.com.servicetrack.domain.ordemServico.ItemOrdemServico
import br.com.servicetrack.domain.ordemServico.vo.ItemOrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.infrastructure.servico.persistence.ServicoEntity
import br.com.servicetrack.infrastructure.usuario.persistence.UsuarioEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "itens_ordem_servico")
class ItemOrdemServicoEntity : PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    lateinit var id: UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false, updatable = false)
    lateinit var ordemServico: OrdemServicoEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false, updatable = false)
    lateinit var servico: ServicoEntity

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    lateinit var valor: BigDecimal

    @Column(name = "feito", nullable = false)
    var feito: Boolean = false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mecanico_responsavel_id", nullable = true)
    var mecanicoResponsavel: UsuarioEntity? = null

    @Column(name = "data_realizacao")
    var dataRealizacao: LocalDateTime? = null

    @Column(name = "observacao", columnDefinition = "TEXT")
    var observacao: String? = null

    @Column(name = "data_criacao", nullable = false, updatable = false)
    lateinit var dataCriacao: LocalDateTime

    @Column(name = "data_atualizacao", nullable = false)
    lateinit var dataAtualizacao: LocalDateTime

    companion object : PanacheCompanion<ItemOrdemServicoEntity> {

        fun de(
            item: ItemOrdemServico,
            ordemServicoRef: OrdemServicoEntity,
        ): ItemOrdemServicoEntity = ItemOrdemServicoEntity().apply {
            id = UUID.fromString(item.id.valor)
            ordemServico = ordemServicoRef
            servico = ServicoEntity().apply {
                this.id = UUID.fromString(item.servicoId.value)
            }
            valor = item.valor.valor
            feito = item.feito
            mecanicoResponsavel = item.mecanicoResponsavelId?.let { uid ->
                UsuarioEntity().apply { this.id = UUID.fromString(uid.valor) }
            }
            dataRealizacao = item.dataRealizacao
            observacao = item.observacao
            dataCriacao = item.dataCriacao
            dataAtualizacao = item.dataCriacao
        }
    }

    fun toDomain(): ItemOrdemServico = ItemOrdemServico.reconstituir(
        id = ItemOrdemServicoId.de(id.toString()),
        servicoId = ServicoId(servico.id.toString()),
        ordemServicoId = OrdemServicoId(ordemServico.id.toString()),
        valor = ValorMonetario(valor),
        feito = feito,
        mecanicoResponsavelId = mecanicoResponsavel?.id?.let { UsuarioId(it.toString()) },
        dataRealizacao = dataRealizacao,
        observacao = observacao,
        dataCriacao = dataCriacao,
        dataAtualizacao = dataAtualizacao,
    )
}
