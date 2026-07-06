package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.LinkDecisaoInvalidoException
import br.com.servicetrack.application.ordemServico.service.support.DecididorOrcamento
import br.com.servicetrack.application.ordemServico.service.support.ResolvedorOrdemServicoPorToken
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class ReprovarOrcamentoViaEmailServiceTest {

    private val resolvedor = mockk<ResolvedorOrdemServicoPorToken>()
    private val decididor = mockk<DecididorOrcamento>()
    private val service = ReprovarOrcamentoViaEmailService(resolvedor, decididor)

    private fun buildOs(status: StatusOrdemServicoEnum): OrdemServico = OrdemServico.reconstituir(
        id = OrdemServicoId.gerar(),
        motivo = "Revisão",
        observacao = "",
        clienteId = UsuarioId.gerar(),
        mecanicoId = UsuarioId.gerar(),
        veiculoId = VeiculoId.gerar(),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
        status = StatusOrdemServico.deEnum(status),
        prazoConclusao = null,
        orcamento = null,
        insumos = mutableListOf(),
        itensServico = mutableListOf(),
    )

    @Test
    fun `deve reprovar com motivo padrao quando token valido`() {
        val os = buildOs(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO)
        val cancelada = buildOs(StatusOrdemServicoEnum.CANCELADA)
        val motivo = slot<String>()
        every { resolvedor.resolver("tok") } returns os
        every { decididor.reprovar(os, capture(motivo)) } returns cancelada

        val result = service.reprovar("tok")

        assertEquals(StatusOrdemServicoEnum.CANCELADA, result.status)
        assertEquals("Reprovado pelo cliente via e-mail", motivo.captured)
    }

    @Test
    fun `deve propagar excecao quando link invalido`() {
        every { resolvedor.resolver("tok") } throws LinkDecisaoInvalidoException()

        assertThrows<LinkDecisaoInvalidoException> { service.reprovar("tok") }

        verify(exactly = 0) { decididor.reprovar(any(), any()) }
    }
}
