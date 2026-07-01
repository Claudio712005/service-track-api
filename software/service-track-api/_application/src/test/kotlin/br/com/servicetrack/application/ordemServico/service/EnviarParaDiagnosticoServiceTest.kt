package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import jakarta.enterprise.event.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class EnviarParaDiagnosticoServiceTest {

    private val repository = mockk<OrdemServicoRepositoryPort>()
    private val jwt = mockk<JwtPort>()
    private val statusEvent = mockk<Event<OrdemServicoStatusAlteradoEvent>>()

    private val service = EnviarParaDiagnosticoService(repository, jwt, statusEvent)

    private val mecanicoId = UsuarioId.gerar()
    private val clienteId = UsuarioId.gerar()

    private fun buildOs(
        id: OrdemServicoId = OrdemServicoId.gerar(),
        status: StatusOrdemServicoEnum = StatusOrdemServicoEnum.RECEBIDA,
        mecanicoId: UsuarioId = this.mecanicoId,
    ): OrdemServico = OrdemServico.reconstituir(
        id = id,
        motivo = "Revisão geral",
        observacao = "",
        clienteId = clienteId,
        mecanicoId = mecanicoId,
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
    fun `deve enviar OS para diagnostico quando mecanico vinculado faz a requisicao`() {
        val os = buildOs()
        every { jwt.getUsuarioId() } returns mecanicoId
        every { repository.buscarPorId(any()) } returns os
        every { repository.atualizar(any()) } answers { firstArg() }
        justRun { statusEvent.fire(any()) }

        val result = service.enviarParaDiagnostico(os.id.valor)

        assertEquals(StatusOrdemServicoEnum.EM_DIAGNOSTICO, result.status)
        verify(exactly = 1) { repository.atualizar(any()) }
        verify(exactly = 1) {
            statusEvent.fire(match { it.novoStatus == StatusOrdemServicoEnum.EM_DIAGNOSTICO })
        }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando OS nao encontrada`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { repository.buscarPorId(any()) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.enviarParaDiagnostico(OrdemServicoId.gerar().valor)
        }

        verify(exactly = 0) { repository.atualizar(any()) }
        verify(exactly = 0) { statusEvent.fire(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando solicitante nao e o mecanico vinculado`() {
        val os = buildOs()
        val outroMecanicoId = UsuarioId.gerar()
        every { jwt.getUsuarioId() } returns outroMecanicoId
        every { repository.buscarPorId(any()) } returns os

        assertThrows<OperacaoNegadaException> {
            service.enviarParaDiagnostico(os.id.valor)
        }

        verify(exactly = 0) { repository.atualizar(any()) }
        verify(exactly = 0) { statusEvent.fire(any()) }
    }
}
