package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.servico.dto.CriarServicoReqDTO
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CriarServicoServiceTest {

    private val repository = mockk<ServicoRepositoryPort>()
    private val service = CriarServicoService(repository)

    @Test
    fun `deve criar servico sem valor de referencia`() {
        every { repository.salvar(any()) } returns Unit

        val req = CriarServicoReqDTO(
            nomeServico = "Troca de Óleo",
            descricaoServico = "Substituição do óleo do motor",
            valorReferencia = null
        )

        val result = service.criarServico(req)

        assertNotNull(result.id)
        assertEquals("Troca de Óleo", result.nomeServico)
        assertEquals("Substituição do óleo do motor", result.descricaoServico)
        assertNull(result.valorReferencia)
        verify(exactly = 1) { repository.salvar(any()) }
    }

    @Test
    fun `deve criar servico com valor de referencia`() {
        every { repository.salvar(any()) } returns Unit

        val req = CriarServicoReqDTO(
            nomeServico = "Alinhamento",
            descricaoServico = "Alinhamento e balanceamento das rodas",
            valorReferencia = BigDecimal("150.00")
        )

        val result = service.criarServico(req)

        assertNotNull(result.id)
        assertEquals("Alinhamento", result.nomeServico)
        assertEquals(BigDecimal("150.00"), result.valorReferencia)
        verify(exactly = 1) { repository.salvar(any()) }
    }
}
