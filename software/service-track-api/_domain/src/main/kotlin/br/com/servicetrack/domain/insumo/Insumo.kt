package br.com.servicetrack.domain.insumo

import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import java.time.LocalDateTime

class Insumo private constructor(
    val id: InsumoId,
    val nome: String,
    val descricao: String,
    val custo: ValorMonetario,
    val estoqueMinimo: Int,
    val dataCriacao: LocalDateTime,
    private var dataAtualizacao: LocalDateTime,
    private var qtdEstoque: Int
) {

    companion object {

        fun criar(
            nome: String,
            descricao: String,
            custo: ValorMonetario,
            qtdEstoqueInicial: Int = 0,
            estoqueMinimo: Int = 0
        ): Insumo {
            require(nome.isNotBlank()) { "Nome do insumo não pode ser vazio" }
            require(qtdEstoqueInicial >= 0) { "Quantidade inicial de estoque não pode ser negativa" }
            require(estoqueMinimo >= 0) { "Estoque mínimo não pode ser negativo" }

            val agora = LocalDateTime.now()

            return Insumo(
                id = InsumoId.gerar(),
                nome = nome,
                descricao = descricao,
                custo = custo,
                estoqueMinimo = estoqueMinimo,
                dataCriacao = agora,
                dataAtualizacao = agora,
                qtdEstoque = qtdEstoqueInicial
            )
        }
    }

    fun obterQtdEstoque(): Int = qtdEstoque

    fun estaAbaixoDoEstoqueMinimo(): Boolean = qtdEstoque < estoqueMinimo

    fun reservar(qtdNecessaria: Int) {
        if (qtdNecessaria <= 0) {
            throw DomainException("A quantidade necessária deve ser maior que zero.")
        }
        if (qtdNecessaria > qtdEstoque) {
            throw DomainException("Quantidade necessária ($qtdNecessaria) excede o estoque disponível ($qtdEstoque).")
        }
        qtdEstoque -= qtdNecessaria
        dataAtualizacao = LocalDateTime.now()
    }

    fun adicionarAoEstoque(qtdAdicional: Int) {
        if (qtdAdicional <= 0) {
            throw DomainException("A quantidade adicional deve ser maior que zero.")
        }
        qtdEstoque += qtdAdicional
        dataAtualizacao = LocalDateTime.now()
    }

    fun calcularCusto(quantidade: Int): ValorMonetario {
        if (quantidade <= 0) {
            throw DomainException("A quantidade necessária deve ser maior que zero.")
        }
        return custo.multiplicar(quantidade.toBigDecimal())
    }
}
