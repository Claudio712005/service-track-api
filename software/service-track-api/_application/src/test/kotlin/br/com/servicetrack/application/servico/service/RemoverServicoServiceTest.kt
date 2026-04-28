package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class RemoverServicoServiceTest {

    private val repository = mockk<ServicoRepositoryPort>()
    private val service = RemoverServicoService(repository)

    @Test
    fun `deve desativar servico existente com sucesso`() {
        val servico = Servico.gerar(
            nomeServico = "Troca de Óleo",
            descricaoServico = "Troca do óleo do motor",
            valorReferencia = ValorMonetario(BigDecimal("120.00"))
        )
        every { repository.buscarPorId(servico.id) } returns servico
        every { repository.desativar(servico.id) } returns Unit

        service.removerServico(servico.id)

        verify(exactly = 1) { repository.buscarPorId(servico.id) }
        verify(exactly = 1) { repository.desativar(servico.id) }
    }

    @Test
    fun `deve desativar servico sem valor de referencia`() {
        val servico = Servico.gerar(
            nomeServico = "Diagnóstico",
            descricaoServico = "Diagnóstico eletrônico",
            valorReferencia = null
        )
        every { repository.buscarPorId(servico.id) } returns servico
        every { repository.desativar(servico.id) } returns Unit

        service.removerServico(servico.id)

        verify(exactly = 1) { repository.desativar(servico.id) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando servico nao encontrado`() {
        val id = ServicoId.gerar()
        every { repository.buscarPorId(id) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.removerServico(id)
        }

        verify(exactly = 1) { repository.buscarPorId(id) }
        verify(exactly = 0) { repository.desativar(any()) }
    }
}
