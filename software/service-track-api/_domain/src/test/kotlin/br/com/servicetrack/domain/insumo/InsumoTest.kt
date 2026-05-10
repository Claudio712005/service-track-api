package br.com.servicetrack.domain.insumo

import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InsumoTest {

    private fun buildInsumo(
        qtdEstoqueInicial: Int = 10,
        estoqueMinimo: Int = 5
    ): Insumo {
        return Insumo.criar(
            nome = "Óleo Motor 5W30",
            descricao = "Óleo para motor 1.0",
            custo = ValorMonetario(BigDecimal("25.00")),
            qtdEstoqueInicial = qtdEstoqueInicial,
            estoqueMinimo = estoqueMinimo
        )
    }

    @Test
    fun `deve criar insumo válido`() {
        val insumo = buildInsumo()

        assertNotNull(insumo)
        assertEquals("Óleo Motor 5W30", insumo.nome)
        assertEquals(10, insumo.obterQtdEstoque())
        assertEquals(5, insumo.estoqueMinimo)
    }

    @Test
    fun `deve lançar exceção ao criar com nome vazio`() {
        val exception = assertThrows<IllegalArgumentException> {
            Insumo.criar(
                nome = "",
                descricao = "desc",
                custo = ValorMonetario(BigDecimal("10.00"))
            )
        }
        assertEquals("Nome do insumo não pode ser vazio", exception.message)
    }

    @Test
    fun `deve lançar exceção ao criar com quantidade inicial negativa`() {
        val exception = assertThrows<IllegalArgumentException> {
            Insumo.criar(
                nome = "Filtro de Ar",
                descricao = "Filtro",
                custo = ValorMonetario(BigDecimal("10.00")),
                qtdEstoqueInicial = -1
            )
        }
        assertEquals("Quantidade inicial de estoque não pode ser negativa", exception.message)
    }

    @Test
    fun `deve lançar exceção ao criar com estoque mínimo negativo`() {
        val exception = assertThrows<IllegalArgumentException> {
            Insumo.criar(
                nome = "Filtro de Ar",
                descricao = "Filtro",
                custo = ValorMonetario(BigDecimal("10.00")),
                estoqueMinimo = -1
            )
        }
        assertEquals("Estoque mínimo não pode ser negativo", exception.message)
    }

    @Test
    fun `deve criar insumo com estoque zerado por padrão`() {
        val insumo = Insumo.criar(
            nome = "Vela de Ignição",
            descricao = "Vela NGK",
            custo = ValorMonetario(BigDecimal("15.00"))
        )
        assertEquals(0, insumo.obterQtdEstoque())
    }

    @Test
    fun `deve reservar estoque com sucesso`() {
        val insumo = buildInsumo(qtdEstoqueInicial = 10)
        insumo.reservar(3)
        assertEquals(7, insumo.obterQtdEstoque())
    }

    @Test
    fun `deve reservar todo o estoque disponível`() {
        val insumo = buildInsumo(qtdEstoqueInicial = 5)
        insumo.reservar(5)
        assertEquals(0, insumo.obterQtdEstoque())
    }

    @Test
    fun `deve lançar exceção ao reservar quantidade zero`() {
        val insumo = buildInsumo()
        val exception = assertThrows<DomainException> {
            insumo.reservar(0)
        }
        assertEquals("A quantidade necessária deve ser maior que zero.", exception.message)
    }

    @Test
    fun `deve lançar exceção ao reservar quantidade negativa`() {
        val insumo = buildInsumo()
        val exception = assertThrows<DomainException> {
            insumo.reservar(-1)
        }
        assertEquals("A quantidade necessária deve ser maior que zero.", exception.message)
    }

    @Test
    fun `deve lançar exceção ao reservar mais do que o estoque disponível`() {
        val insumo = buildInsumo(qtdEstoqueInicial = 5)
        val exception = assertThrows<DomainException> {
            insumo.reservar(10)
        }
        assertTrue(exception.message!!.contains("excede o estoque disponível"))
    }

    @Test
    fun `deve adicionar ao estoque com sucesso`() {
        val insumo = buildInsumo(qtdEstoqueInicial = 5)
        insumo.adicionarAoEstoque(3)
        assertEquals(8, insumo.obterQtdEstoque())
    }

    @Test
    fun `deve lançar exceção ao adicionar quantidade zero ao estoque`() {
        val insumo = buildInsumo()
        val exception = assertThrows<DomainException> {
            insumo.adicionarAoEstoque(0)
        }
        assertEquals("A quantidade adicional deve ser maior que zero.", exception.message)
    }

    @Test
    fun `deve lançar exceção ao adicionar quantidade negativa ao estoque`() {
        val insumo = buildInsumo()
        val exception = assertThrows<DomainException> {
            insumo.adicionarAoEstoque(-5)
        }
        assertEquals("A quantidade adicional deve ser maior que zero.", exception.message)
    }

    @Test
    fun `deve calcular custo total corretamente`() {
        val insumo = buildInsumo()
        val custo = insumo.calcularCusto(2)
        assertEquals(BigDecimal("50.00"), custo.valor)
    }

    @Test
    fun `deve calcular custo para quantidade unitária`() {
        val insumo = buildInsumo()
        val custo = insumo.calcularCusto(1)
        assertEquals(BigDecimal("25.00"), custo.valor)
    }

    @Test
    fun `deve lançar exceção ao calcular custo com quantidade zero`() {
        val insumo = buildInsumo()
        val exception = assertThrows<DomainException> {
            insumo.calcularCusto(0)
        }
        assertEquals("A quantidade necessária deve ser maior que zero.", exception.message)
    }

    @Test
    fun `deve identificar quando estoque está abaixo do mínimo`() {
        val insumo = buildInsumo(qtdEstoqueInicial = 3, estoqueMinimo = 5)
        assertTrue(insumo.estaAbaixoDoEstoqueMinimo())
    }

    @Test
    fun `não deve indicar abaixo do mínimo quando estoque é suficiente`() {
        val insumo = buildInsumo(qtdEstoqueInicial = 10, estoqueMinimo = 5)
        assertFalse(insumo.estaAbaixoDoEstoqueMinimo())
    }

    @Test
    fun `não deve indicar abaixo do mínimo quando estoque é igual ao mínimo`() {
        val insumo = buildInsumo(qtdEstoqueInicial = 5, estoqueMinimo = 5)
        assertFalse(insumo.estaAbaixoDoEstoqueMinimo())
    }

    @Test
    fun `deve indicar abaixo do mínimo após reserva`() {
        val insumo = buildInsumo(qtdEstoqueInicial = 6, estoqueMinimo = 5)
        assertFalse(insumo.estaAbaixoDoEstoqueMinimo())
        insumo.reservar(2)
        assertTrue(insumo.estaAbaixoDoEstoqueMinimo())
    }

    @Test
    fun `deve deixar de estar abaixo do mínimo após reposição`() {
        val insumo = buildInsumo(qtdEstoqueInicial = 2, estoqueMinimo = 5)
        assertTrue(insumo.estaAbaixoDoEstoqueMinimo())
        insumo.adicionarAoEstoque(10)
        assertFalse(insumo.estaAbaixoDoEstoqueMinimo())
    }

    @Test
    fun `deve reconstituir insumo a partir de dados de persistencia`() {
        val id = br.com.servicetrack.domain.insumo.vo.InsumoId.gerar()
        val agora = java.time.LocalDateTime.now()

        val insumo = Insumo.reconstituir(
            id = id,
            nome = "Filtro de Combustível",
            descricao = "Filtro para motores flex",
            custo = ValorMonetario(BigDecimal("35.00")),
            estoqueMinimo = 2,
            qtdEstoque = 10,
            dataCriacao = agora,
            dataAtualizacao = agora
        )

        assertEquals(id, insumo.id)
        assertEquals("Filtro de Combustível", insumo.nome)
        assertEquals(10, insumo.obterQtdEstoque())
        assertEquals(2, insumo.estoqueMinimo)
    }
}
