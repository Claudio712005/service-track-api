package br.com.servicetrack.application.ordemServico.service.support

import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.application.notificacao.dto.EnfileirarNotificacaoCommand
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.orcamento.Orcamento
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.enterprise.event.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class DecididorOrcamentoTest {

    private val osRepository = mockk<OrdemServicoRepositoryPort>()
    private val insumoRepository = mockk<InsumoRepositoryPort>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val enfileirar = mockk<EnfileirarNotificacaoUseCase>()
    private val statusEvent = mockk<Event<OrdemServicoStatusAlteradoEvent>>()

    private val decididor = DecididorOrcamento(
        osRepository, insumoRepository, usuarioRepository, enfileirar, statusEvent,
    )

    private val mecanicoId = UsuarioId.gerar()
    private val clienteId = UsuarioId.gerar()

    private fun buildOs(insumos: List<InsumoId> = emptyList()): OrdemServico = OrdemServico.reconstituir(
        id = OrdemServicoId.gerar(),
        motivo = "Revisão",
        observacao = "",
        clienteId = clienteId,
        mecanicoId = mecanicoId,
        veiculoId = VeiculoId.gerar(),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
        status = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO),
        prazoConclusao = null,
        orcamento = Orcamento.gerar(ValorMonetario(BigDecimal("150.00")), ValorMonetario(BigDecimal("80.00"))),
        insumos = insumos.toMutableList(),
        itensServico = mutableListOf(),
    )

    private fun buildInsumo(id: InsumoId): Insumo = Insumo.reconstituir(
        id = id,
        nome = "Filtro",
        descricao = "Filtro 1.0",
        custo = ValorMonetario(BigDecimal("25.00")),
        estoqueMinimo = 2,
        qtdEstoque = 5,
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
    )

    @Test
    fun `aprovar deve transitar para EM_EXECUCAO e notificar o mecanico`() {
        val os = buildOs()
        every { osRepository.atualizar(any()) } answers { firstArg() }
        every { usuarioRepository.buscarPorId(clienteId) } returns null
        justRun { statusEvent.fire(any()) }
        val cmd = slot<EnfileirarNotificacaoCommand>()
        every { enfileirar.executar(capture(cmd)) } returns NotificacaoId.gerar()

        val result = decididor.aprovar(os)

        assertEquals(StatusOrdemServicoEnum.EM_EXECUCAO, result.obterStatus())
        assertEquals(mecanicoId, cmd.captured.destinatario)
        assertEquals(TipoConteudoNotificacao.DECISAO_ORCAMENTO_OS, cmd.captured.tipoConteudoNotificacao)
        assertEquals("aprovado", cmd.captured.variaveis["decisao"])
        verify(exactly = 1) { statusEvent.fire(match { it.novoStatus == StatusOrdemServicoEnum.EM_EXECUCAO }) }
    }

    @Test
    fun `reprovar deve repor estoque, cancelar e notificar o mecanico`() {
        val insumoId = InsumoId.gerar()
        val insumo = buildInsumo(insumoId)
        val os = buildOs(insumos = listOf(insumoId, insumoId))
        every { insumoRepository.buscarPorId(insumoId) } returns insumo
        justRun { insumoRepository.atualizar(any()) }
        every { osRepository.atualizar(any()) } answers { firstArg() }
        every { usuarioRepository.buscarPorId(clienteId) } returns null
        justRun { statusEvent.fire(any()) }
        val cmd = slot<EnfileirarNotificacaoCommand>()
        every { enfileirar.executar(capture(cmd)) } returns NotificacaoId.gerar()

        val result = decididor.reprovar(os, "Muito caro")

        assertEquals(StatusOrdemServicoEnum.CANCELADA, result.obterStatus())
        assertEquals("reprovado", cmd.captured.variaveis["decisao"])
        assertEquals("Muito caro", cmd.captured.variaveis["motivo"])
        verify(exactly = 1) { insumoRepository.atualizar(insumo) }
    }
}
