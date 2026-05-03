package br.com.servicetrack.infrastructure.ordemServico.persistence

import br.com.servicetrack.domain.orcamento.Orcamento
import br.com.servicetrack.domain.orcamento.vo.OrcamentoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.math.BigDecimal
import java.time.LocalDateTime

@Embeddable
class OrcamentoEmbeddable {

    @Column(name = "orcamento_id")
    var orcamentoId: String? = null

    @Column(name = "orcamento_data_criacao")
    var dataCriacao: LocalDateTime? = null

    @Column(name = "orcamento_data_atualizacao")
    var dataAtualizacao: LocalDateTime? = null

    @Column(name = "orcamento_custo_mao_de_obra", precision = 12, scale = 2)
    var custoMaoDeObra: BigDecimal? = null

    @Column(name = "orcamento_custo_insumos", precision = 12, scale = 2)
    var custoInsumos: BigDecimal? = null

    @Column(name = "orcamento_aprovado")
    var aprovado: Boolean? = null

    @Column(name = "orcamento_observacao", columnDefinition = "TEXT")
    var observacao: String? = null

    companion object {

        fun de(orcamento: Orcamento): OrcamentoEmbeddable = OrcamentoEmbeddable().apply {
            orcamentoId = orcamento.id.valor
            dataCriacao = orcamento.dataCriacao
            dataAtualizacao = orcamento.dataCriacao
            custoMaoDeObra = orcamento.custoMaoDeObra.valor
            custoInsumos = orcamento.custoInsumos.valor
            aprovado = orcamento.estaAprovado()
            observacao = orcamento.obterObservacao()
        }
    }

    fun toDomainOrNull(): Orcamento? {
        val id = orcamentoId ?: return null
        return Orcamento.reconstituir(
            id = OrcamentoId.de(id),
            dataCriacao = dataCriacao!!,
            dataAtualizacao = dataAtualizacao!!,
            custoMaoDeObra = ValorMonetario(custoMaoDeObra!!),
            custoInsumos = ValorMonetario(custoInsumos!!),
            aprovado = aprovado!!,
            observacao = observacao ?: "",
        )
    }
}
