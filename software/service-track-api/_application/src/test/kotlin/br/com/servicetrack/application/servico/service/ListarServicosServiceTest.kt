package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ListarServicosServiceTest {

    private val repository = mockk<ServicoRepositoryPort>()
    private val service = ListarServicosService(repository)

    @Test
    fun `deve retornar lista completa de servicos`() {
        val servicos = listOf(
            Servico.gerar("Troca de Óleo", "Troca do óleo do motor", ValorMonetario(BigDecimal("120.00"))),
            Servico.gerar("Alinhamento", "Alinhamento e balanceamento", null)
        )
        every { repository.listarTodos() } returns servicos

        val result = service.listarServicos()

        assertEquals(2, result.size)
        assertEquals("Troca de Óleo", result[0].nomeServico)
        assertEquals(BigDecimal("120.00"), result[0].valorReferencia)
        assertEquals("Alinhamento", result[1].nomeServico)
        assertEquals(null, result[1].valorReferencia)
        verify(exactly = 1) { repository.listarTodos() }
    }

    @Test
    fun `deve retornar lista vazia quando nao ha servicos`() {
        every { repository.listarTodos() } returns emptyList()

        val result = service.listarServicos()

        assertTrue(result.isEmpty())
        verify(exactly = 1) { repository.listarTodos() }
    }

    @Test
    fun `deve retornar lista resumida de servicos`() {
        val servicos = listOf(
            Servico.gerar("Troca de Óleo", "Troca do óleo do motor", ValorMonetario(BigDecimal("120.00"))),
            Servico.gerar("Revisão Geral", "Revisão completa do veículo", null)
        )
        every { repository.listarTodos() } returns servicos

        val result = service.listarResumidos()

        assertEquals(2, result.size)
        assertEquals("Troca de Óleo", result[0].nomeServico)
        assertEquals("Troca do óleo do motor", result[0].descricaoServico)
        assertEquals("Revisão Geral", result[1].nomeServico)
        verify(exactly = 1) { repository.listarTodos() }
    }

    @Test
    fun `deve retornar lista resumida vazia quando nao ha servicos`() {
        every { repository.listarTodos() } returns emptyList()

        val result = service.listarResumidos()

        assertTrue(result.isEmpty())
        verify(exactly = 1) { repository.listarTodos() }
    }
}
