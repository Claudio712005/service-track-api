package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ListarInsumosServiceTest {

    private val repository = mockk<InsumoRepositoryPort>()
    private val service = ListarInsumosService(repository)

    @Test
    fun `deve retornar lista completa de insumos`() {
        val insumos = listOf(
            Insumo.criar("Filtro de Óleo", "Filtro motor 1.0", ValorMonetario(BigDecimal("25.50")), 10, 2),
            Insumo.criar("Pastilha de Freio", "Pastilha dianteira", ValorMonetario(BigDecimal("89.90")), 5, 1)
        )
        every { repository.listarTodos() } returns insumos

        val result = service.listarInsumos()

        assertEquals(2, result.size)
        assertEquals("Filtro de Óleo", result[0].nome)
        assertEquals(BigDecimal("25.50"), result[0].custo)
        assertEquals(10, result[0].qtdEstoque)
        assertEquals("Pastilha de Freio", result[1].nome)
        assertEquals(5, result[1].qtdEstoque)
        verify(exactly = 1) { repository.listarTodos() }
    }

    @Test
    fun `deve retornar lista vazia quando nao ha insumos`() {
        every { repository.listarTodos() } returns emptyList()

        val result = service.listarInsumos()

        assertTrue(result.isEmpty())
        verify(exactly = 1) { repository.listarTodos() }
    }

    @Test
    fun `deve retornar lista resumida de insumos`() {
        val insumos = listOf(
            Insumo.criar("Filtro de Óleo", "Filtro motor 1.0", ValorMonetario(BigDecimal("25.50")), 10, 2),
            Insumo.criar("Vela de Ignição", "Vela NGK compatível", ValorMonetario(BigDecimal("15.00")), 20, 4)
        )
        every { repository.listarTodos() } returns insumos

        val result = service.listarResumidos()

        assertEquals(2, result.size)
        assertEquals("Filtro de Óleo", result[0].nome)
        assertEquals("Filtro motor 1.0", result[0].descricao)
        assertEquals("Vela de Ignição", result[1].nome)
        verify(exactly = 1) { repository.listarTodos() }
    }

    @Test
    fun `deve retornar lista resumida vazia quando nao ha insumos`() {
        every { repository.listarTodos() } returns emptyList()

        val result = service.listarResumidos()

        assertTrue(result.isEmpty())
        verify(exactly = 1) { repository.listarTodos() }
    }
}
