package br.com.servicetrack.infrastructure.servico.persistence

import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "servicos")
class ServicoEntity : PanacheEntityBase {

    @jakarta.persistence.Id
    @Column(name = "id", nullable = false, updatable = false)
    lateinit var id: UUID

    @Column(name = "nome_servico", nullable = false, length = 150)
    lateinit var nomeServico: String

    @Column(name = "descricao_servico", nullable = false, columnDefinition = "TEXT")
    lateinit var descricaoServico: String

    @Column(name = "valor_referencia", precision = 12, scale = 2)
    var valorReferencia: BigDecimal? = null

    @Column(name = "data_criacao", nullable = false, updatable = false)
    lateinit var dataCriacao: LocalDateTime

    @Column(name = "data_atualizacao", nullable = false)
    lateinit var dataAtualizacao: LocalDateTime

    companion object : PanacheCompanion<ServicoEntity> {

        fun de(servico: Servico): ServicoEntity = ServicoEntity().apply {
            id = UUID.fromString(servico.id.valor)
            nomeServico = servico.nomeServico
            descricaoServico = servico.descricaoServico
            valorReferencia = servico.valorReferencia?.valor
            dataCriacao = servico.dataCriacao
            dataAtualizacao = servico.dataCriacao
        }
    }

    fun toDomain(): Servico = Servico.reconstituir(
        id = ServicoId(id.toString()),
        nomeServico = nomeServico,
        descricaoServico = descricaoServico,
        valorReferencia = valorReferencia?.let { ValorMonetario(it) },
        dataCriacao = dataCriacao,
        dataAtualizacao = dataAtualizacao,
    )
}
