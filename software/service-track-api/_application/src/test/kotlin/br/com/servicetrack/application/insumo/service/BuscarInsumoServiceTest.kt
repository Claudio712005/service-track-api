package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BuscarInsumoServiceTest {

    private val repository = mockk<InsumoRepositoryPort>()
    private val service = BuscarInsumoService(repository)

    @Test
    fun `deve retornar insumo quando encontrado pelo id`() {
        val insumo = Insumo.criar(
            nome = "Filtro de Óleo",
            descricao = "Filtro para motor 1.0",
            custo = ValorMonetario(BigDecimal("25.50")),
            qtdEstoqueInicial = 10,
            estoqueMinimo = 2
        )
        every { repository.buscarPorId(insumo.id) } returns insumo

        val result = service.buscarInsumo(insumo.id)

        assertNotNull(result.id)
        assertEquals("Filtro de Óleo", result.nome)
        assertEquals("Filtro para motor 1.0", result.descricao)
        assertEquals(BigDecimal("25.50"), result.custo)
        assertEquals(10, result.qtdEstoque)
        assertEquals(2, result.estoqueMinimo)
        verify(exactly = 1) { repository.buscarPorId(insumo.id) }
    }

    @Test
    fun `deve retornar insumo com estoque zerado`() {
        val insumo = Insumo.criar(
            nome = "Pastilha de Freio",
            descricao = "Pastilha dianteira",
            custo = ValorMonetario(BigDecimal("89.90")),
            qtdEstoqueInicial = 0,
            estoqueMinimo = 0
        )
        every { repository.buscarPorId(insumo.id) } returns insumo

        val result = service.buscarInsumo(insumo.id)

        assertNotNull(result.id)
        assertEquals(0, result.qtdEstoque)
        verify(exactly = 1) { repository.buscarPorId(insumo.id) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando insumo nao existe`() {
        val id = InsumoId.gerar()
        every { repository.buscarPorId(id) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.buscarInsumo(id)
        }

        verify(exactly = 1) { repository.buscarPorId(id) }
    }
}
