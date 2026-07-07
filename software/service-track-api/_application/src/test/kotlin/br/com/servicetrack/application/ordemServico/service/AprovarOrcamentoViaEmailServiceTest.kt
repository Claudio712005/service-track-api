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
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class AprovarOrcamentoViaEmailServiceTest {

    private val resolvedor = mockk<ResolvedorOrdemServicoPorToken>()
    private val decididor = mockk<DecididorOrcamento>()
    private val service = AprovarOrcamentoViaEmailService(resolvedor, decididor)

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
    fun `deve aprovar quando token valido`() {
        val os = buildOs(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO)
        val aprovada = buildOs(StatusOrdemServicoEnum.EM_EXECUCAO)
        every { resolvedor.resolver("tok") } returns os
        every { decididor.aprovar(os) } returns aprovada

        val result = service.aprovar("tok")

        assertEquals(StatusOrdemServicoEnum.EM_EXECUCAO, result.status)
        verify(exactly = 1) { decididor.aprovar(os) }
    }

    @Test
    fun `deve propagar excecao quando link invalido`() {
        every { resolvedor.resolver("tok") } throws LinkDecisaoInvalidoException()

        assertThrows<LinkDecisaoInvalidoException> { service.aprovar("tok") }

        verify(exactly = 0) { decididor.aprovar(any()) }
    }
}
