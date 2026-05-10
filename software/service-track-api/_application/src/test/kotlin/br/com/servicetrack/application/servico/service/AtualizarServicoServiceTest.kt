package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.servico.dto.AtualizarServicoReqDTO
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class AtualizarServicoServiceTest {

    private val repository = mockk<ServicoRepositoryPort>()
    private val service = AtualizarServicoService(repository)

    @Test
    fun `deve atualizar todos os campos do servico`() {
        val existente = Servico.gerar(
            nomeServico = "Troca de Óleo",
            descricaoServico = "Troca simples",
            valorReferencia = ValorMonetario(BigDecimal("100.00"))
        )
        every { repository.buscarPorId(existente.id) } returns existente
        every { repository.atualizar(any()) } returns Unit

        val req = AtualizarServicoReqDTO(
            nomeServico = "Troca de Óleo Sintético",
            descricaoServico = "Troca com óleo sintético premium",
            valorReferencia = BigDecimal("200.00")
        )

        val result = service.atualizarServico(existente.id, req)

        assertEquals("Troca de Óleo Sintético", result.nomeServico)
        assertEquals("Troca com óleo sintético premium", result.descricaoServico)
        assertEquals(BigDecimal("200.00"), result.valorReferencia)
        verify(exactly = 1) { repository.buscarPorId(existente.id) }
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve manter valores existentes quando campos nulos enviados`() {
        val existente = Servico.gerar(
            nomeServico = "Alinhamento",
            descricaoServico = "Alinhamento das rodas",
            valorReferencia = ValorMonetario(BigDecimal("80.00"))
        )
        every { repository.buscarPorId(existente.id) } returns existente
        every { repository.atualizar(any()) } returns Unit

        val req = AtualizarServicoReqDTO(
            nomeServico = null,
            descricaoServico = null,
            valorReferencia = null
        )

        val result = service.atualizarServico(existente.id, req)

        assertEquals("Alinhamento", result.nomeServico)
        assertEquals("Alinhamento das rodas", result.descricaoServico)
        assertEquals(BigDecimal("80.00"), result.valorReferencia)
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve atualizar apenas o nome do servico`() {
        val existente = Servico.gerar(
            nomeServico = "Balanceamento",
            descricaoServico = "Balanceamento das rodas",
            valorReferencia = ValorMonetario(BigDecimal("60.00"))
        )
        every { repository.buscarPorId(existente.id) } returns existente
        every { repository.atualizar(any()) } returns Unit

        val req = AtualizarServicoReqDTO(
            nomeServico = "Balanceamento Computadorizado",
            descricaoServico = null,
            valorReferencia = null
        )

        val result = service.atualizarServico(existente.id, req)

        assertEquals("Balanceamento Computadorizado", result.nomeServico)
        assertEquals("Balanceamento das rodas", result.descricaoServico)
        assertEquals(BigDecimal("60.00"), result.valorReferencia)
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve atualizar valor de referencia para nulo quando informado null`() {
        val existente = Servico.gerar(
            nomeServico = "Diagnóstico",
            descricaoServico = "Diagnóstico eletrônico",
            valorReferencia = ValorMonetario(BigDecimal("50.00"))
        )
        every { repository.buscarPorId(existente.id) } returns existente
        every { repository.atualizar(any()) } returns Unit

        val req = AtualizarServicoReqDTO(
            nomeServico = null,
            descricaoServico = null,
            valorReferencia = null
        )

        val result = service.atualizarServico(existente.id, req)

        assertEquals(BigDecimal("50.00"), result.valorReferencia)
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando servico nao encontrado`() {
        val id = ServicoId.gerar()
        every { repository.buscarPorId(id) } returns null

        val req = AtualizarServicoReqDTO(
            nomeServico = "Novo Nome",
            descricaoServico = null,
            valorReferencia = null
        )

        assertThrows<EntidadeNaoEncontradaException> {
            service.atualizarServico(id, req)
        }

        verify(exactly = 1) { repository.buscarPorId(id) }
        verify(exactly = 0) { repository.atualizar(any()) }
    }
}
