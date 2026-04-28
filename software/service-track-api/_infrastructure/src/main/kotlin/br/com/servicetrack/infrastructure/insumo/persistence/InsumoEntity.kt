package br.com.servicetrack.infrastructure.insumo.persistence

import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "insumos")
class InsumoEntity : PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    lateinit var id: UUID

    @Column(name = "nome", nullable = false, length = 150)
    lateinit var nome: String

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    lateinit var descricao: String

    @Column(name = "custo", nullable = false, precision = 12, scale = 2)
    lateinit var custo: BigDecimal

    @Column(name = "estoque_minimo", nullable = false)
    var estoqueMinimo: Int = 0

    @Column(name = "qtd_estoque", nullable = false)
    var qtdEstoque: Int = 0

    @Column(name = "data_criacao", nullable = false, updatable = false)
    lateinit var dataCriacao: LocalDateTime

    @Column(name = "data_atualizacao", nullable = false)
    lateinit var dataAtualizacao: LocalDateTime

    @Column(name = "ativo", nullable = false)
    var ativo: Boolean = true

    companion object : PanacheCompanion<InsumoEntity> {

        fun de(insumo: Insumo): InsumoEntity = InsumoEntity().apply {
            id = UUID.fromString(insumo.id.valor)
            nome = insumo.nome
            descricao = insumo.descricao
            custo = insumo.custo.valor
            estoqueMinimo = insumo.estoqueMinimo
            qtdEstoque = insumo.obterQtdEstoque()
            dataCriacao = insumo.dataCriacao
            dataAtualizacao = insumo.dataCriacao
            ativo = insumo.estaAtivo()
        }
    }

    fun toDomain(): Insumo = Insumo.reconstituir(
        id = InsumoId.de(id.toString()),
        nome = nome,
        descricao = descricao,
        custo = ValorMonetario(custo),
        estoqueMinimo = estoqueMinimo,
        qtdEstoque = qtdEstoque,
        dataCriacao = dataCriacao,
        dataAtualizacao = dataAtualizacao,
        ativo = ativo,
    )
}
