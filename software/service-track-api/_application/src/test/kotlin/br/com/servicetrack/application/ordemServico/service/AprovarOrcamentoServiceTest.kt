package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.orcamento.Orcamento
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime

class AprovarOrcamentoServiceTest {

    private val repository = mockk<OrdemServicoRepositoryPort>()
    private val jwt = mockk<JwtPort>()

    private val service = AprovarOrcamentoService(repository, jwt)

    private val mecanicoId = UsuarioId.gerar()
    private val clienteId = UsuarioId.gerar()

    private fun buildOrcamento() = Orcamento.gerar(
        custoMaoDeObra = ValorMonetario(BigDecimal("150.00")),
        custoInsumos = ValorMonetario(BigDecimal("80.00")),
    )

    private fun buildOs(
        status: StatusOrdemServicoEnum = StatusOrdemServicoEnum.AGUARDANDO_APROVACAO,
        clienteId: UsuarioId = this.clienteId,
        comOrcamento: Boolean = true,
    ): OrdemServico = OrdemServico.reconstituir(
        id = OrdemServicoId.gerar(),
        motivo = "Revisão geral",
        observacao = "",
        clienteId = clienteId,
        mecanicoId = mecanicoId,
        veiculoId = VeiculoId.gerar(),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
        status = StatusOrdemServico.deEnum(status),
        prazoConclusao = null,
        orcamento = if (comOrcamento) buildOrcamento() else null,
        insumos = mutableListOf(),
        itensServico = mutableListOf(),
    )

    @Test
    fun `deve aprovar orcamento quando cliente titular faz a requisicao`() {
        val os = buildOs()
        every { jwt.getUsuarioId() } returns clienteId
        every { repository.buscarPorId(any()) } returns os
        every { repository.atualizar(any()) } answers { firstArg() }

        val result = service.aprovarOrcamento(os.id.valor)

        assertEquals(StatusOrdemServicoEnum.EM_EXECUCAO, result.status)
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando OS nao encontrada`() {
        every { jwt.getUsuarioId() } returns clienteId
        every { repository.buscarPorId(any()) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.aprovarOrcamento(OrdemServicoId.gerar().valor)
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando solicitante nao e o cliente titular`() {
        val os = buildOs()
        val outroClienteId = UsuarioId.gerar()
        every { jwt.getUsuarioId() } returns outroClienteId
        every { repository.buscarPorId(any()) } returns os

        assertThrows<OperacaoNegadaException> {
            service.aprovarOrcamento(os.id.valor)
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }
}
