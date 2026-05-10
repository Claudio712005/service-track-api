package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BuscarServicoServiceTest {

    private val repository = mockk<ServicoRepositoryPort>()
    private val service = BuscarServicoService(repository)

    @Test
    fun `deve retornar servico quando encontrado pelo id`() {
        val servico = Servico.gerar(
            nomeServico = "Troca de Óleo",
            descricaoServico = "Substituição do óleo do motor",
            valorReferencia = ValorMonetario(BigDecimal("120.00"))
        )
        every { repository.buscarPorId(servico.id) } returns servico

        val result = service.buscarServico(servico.id)

        assertNotNull(result.id)
        assertEquals("Troca de Óleo", result.nomeServico)
        assertEquals("Substituição do óleo do motor", result.descricaoServico)
        assertEquals(BigDecimal("120.00"), result.valorReferencia)
        verify(exactly = 1) { repository.buscarPorId(servico.id) }
    }

    @Test
    fun `deve retornar servico sem valor de referencia`() {
        val servico = Servico.gerar(
            nomeServico = "Diagnóstico",
            descricaoServico = "Diagnóstico eletrônico do veículo",
            valorReferencia = null
        )
        every { repository.buscarPorId(servico.id) } returns servico

        val result = service.buscarServico(servico.id)

        assertNotNull(result.id)
        assertEquals("Diagnóstico", result.nomeServico)
        assertNull(result.valorReferencia)
        verify(exactly = 1) { repository.buscarPorId(servico.id) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando servico nao existe`() {
        val id = ServicoId.gerar()
        every { repository.buscarPorId(id) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.buscarServico(id)
        }

        verify(exactly = 1) { repository.buscarPorId(id) }
    }
}
