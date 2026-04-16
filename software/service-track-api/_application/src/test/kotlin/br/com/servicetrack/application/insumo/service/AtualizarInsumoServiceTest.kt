package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.insumo.dto.AtualizarInsumoReqDTO
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class AtualizarInsumoServiceTest {

    private val repository = mockk<InsumoRepositoryPort>()
    private val service = AtualizarInsumoService(repository)

    @Test
    fun `deve atualizar todos os campos do insumo`() {
        val existente = Insumo.criar(
            nome = "Filtro de Óleo",
            descricao = "Filtro motor 1.0",
            custo = ValorMonetario(BigDecimal("25.50")),
            qtdEstoqueInicial = 10,
            estoqueMinimo = 2
        )
        every { repository.buscarPorId(existente.id) } returns existente
        every { repository.atualizar(any()) } returns Unit

        val req = AtualizarInsumoReqDTO(
            nome = "Filtro de Óleo Premium",
            descricao = "Filtro motor 1.0 e 1.4",
            custo = BigDecimal("35.00"),
            estoqueMinimo = 5
        )

        val result = service.atualizarInsumo(existente.id, req)

        assertEquals("Filtro de Óleo Premium", result.nome)
        assertEquals("Filtro motor 1.0 e 1.4", result.descricao)
        assertEquals(BigDecimal("35.00"), result.custo)
        assertEquals(5, result.estoqueMinimo)
        verify(exactly = 1) { repository.buscarPorId(existente.id) }
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve manter valores existentes quando campos nulos enviados`() {
        val existente = Insumo.criar(
            nome = "Pastilha de Freio",
            descricao = "Pastilha dianteira",
            custo = ValorMonetario(BigDecimal("89.90")),
            qtdEstoqueInicial = 5,
            estoqueMinimo = 1
        )
        every { repository.buscarPorId(existente.id) } returns existente
        every { repository.atualizar(any()) } returns Unit

        val req = AtualizarInsumoReqDTO(
            nome = null,
            descricao = null,
            custo = null,
            estoqueMinimo = null
        )

        val result = service.atualizarInsumo(existente.id, req)

        assertEquals("Pastilha de Freio", result.nome)
        assertEquals("Pastilha dianteira", result.descricao)
        assertEquals(BigDecimal("89.90"), result.custo)
        assertEquals(1, result.estoqueMinimo)
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve atualizar apenas o custo do insumo`() {
        val existente = Insumo.criar(
            nome = "Vela de Ignição",
            descricao = "Vela NGK",
            custo = ValorMonetario(BigDecimal("15.00")),
            qtdEstoqueInicial = 20,
            estoqueMinimo = 4
        )
        every { repository.buscarPorId(existente.id) } returns existente
        every { repository.atualizar(any()) } returns Unit

        val req = AtualizarInsumoReqDTO(
            nome = null,
            descricao = null,
            custo = BigDecimal("18.50"),
            estoqueMinimo = null
        )

        val result = service.atualizarInsumo(existente.id, req)

        assertEquals("Vela de Ignição", result.nome)
        assertEquals(BigDecimal("18.50"), result.custo)
        assertEquals(4, result.estoqueMinimo)
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve atualizar apenas o estoque minimo do insumo`() {
        val existente = Insumo.criar(
            nome = "Óleo de Motor",
            descricao = "Óleo sintético 5W30",
            custo = ValorMonetario(BigDecimal("45.00")),
            qtdEstoqueInicial = 8,
            estoqueMinimo = 2
        )
        every { repository.buscarPorId(existente.id) } returns existente
        every { repository.atualizar(any()) } returns Unit

        val req = AtualizarInsumoReqDTO(
            nome = null,
            descricao = null,
            custo = null,
            estoqueMinimo = 3
        )

        val result = service.atualizarInsumo(existente.id, req)

        assertEquals(3, result.estoqueMinimo)
        assertEquals(BigDecimal("45.00"), result.custo)
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando insumo nao encontrado`() {
        val id = InsumoId.gerar()
        every { repository.buscarPorId(id) } returns null

        val req = AtualizarInsumoReqDTO(
            nome = "Novo Nome",
            descricao = null,
            custo = null,
            estoqueMinimo = null
        )

        assertThrows<EntidadeNaoEncontradaException> {
            service.atualizarInsumo(id, req)
        }

        verify(exactly = 1) { repository.buscarPorId(id) }
        verify(exactly = 0) { repository.atualizar(any()) }
    }
}
