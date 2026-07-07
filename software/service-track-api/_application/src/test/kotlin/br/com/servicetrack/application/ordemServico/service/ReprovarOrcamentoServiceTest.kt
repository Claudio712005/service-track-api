package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.application.ordemServico.dto.request.ReprovarOrcamentoReqDTO
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.service.support.DecididorOrcamento
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import jakarta.enterprise.event.Event
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
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
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime

class ReprovarOrcamentoServiceTest {

    private val osRepository = mockk<OrdemServicoRepositoryPort>()
    private val insumoRepository = mockk<InsumoRepositoryPort>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>(relaxed = true)
    private val enfileirar = mockk<EnfileirarNotificacaoUseCase>(relaxed = true)
    private val jwt = mockk<JwtPort>()
    private val statusEvent = mockk<Event<OrdemServicoStatusAlteradoEvent>>(relaxed = true)

    private val decididor = DecididorOrcamento(osRepository, insumoRepository, usuarioRepository, enfileirar, statusEvent)
    private val service = ReprovarOrcamentoService(osRepository, jwt, decididor)

    private val mecanicoId = UsuarioId.gerar()
    private val clienteId = UsuarioId.gerar()

    private fun buildOrcamento() = Orcamento.gerar(
        custoMaoDeObra = ValorMonetario(BigDecimal("150.00")),
        custoInsumos = ValorMonetario(BigDecimal("80.00")),
    )

    private fun buildInsumo(id: InsumoId = InsumoId.gerar()): Insumo = Insumo.reconstituir(
        id = id,
        nome = "Filtro de Óleo",
        descricao = "Filtro motor 1.0",
        custo = ValorMonetario(BigDecimal("25.00")),
        estoqueMinimo = 2,
        qtdEstoque = 10,
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
    )

    private fun buildOs(
        status: StatusOrdemServicoEnum = StatusOrdemServicoEnum.AGUARDANDO_APROVACAO,
        clienteId: UsuarioId = this.clienteId,
        insumos: List<InsumoId> = emptyList(),
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
        orcamento = buildOrcamento(),
        insumos = insumos.toMutableList(),
        itensServico = mutableListOf(),
    )

    @Test
    fun `deve reprovar orcamento e retornar insumos ao estoque`() {
        val insumoId = InsumoId.gerar()
        val insumo = buildInsumo(insumoId)
        val os = buildOs(insumos = listOf(insumoId))
        every { jwt.getUsuarioId() } returns clienteId
        every { osRepository.buscarPorId(any()) } returns os
        every { insumoRepository.buscarPorId(insumoId) } returns insumo
        justRun { insumoRepository.atualizar(any()) }
        every { osRepository.atualizar(any()) } answers { firstArg() }

        val result = service.reprovarOrcamento(os.id.valor, ReprovarOrcamentoReqDTO("Valores altos demais"))

        assertEquals(StatusOrdemServicoEnum.CANCELADA, result.status)
        verify(exactly = 1) { insumoRepository.atualizar(insumo) }
        verify(exactly = 1) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve reprovar orcamento sem insumos quando OS nao tem insumos associados`() {
        val os = buildOs()
        every { jwt.getUsuarioId() } returns clienteId
        every { osRepository.buscarPorId(any()) } returns os
        every { osRepository.atualizar(any()) } answers { firstArg() }

        val result = service.reprovarOrcamento(os.id.valor, ReprovarOrcamentoReqDTO("Valores altos demais"))

        assertEquals(StatusOrdemServicoEnum.CANCELADA, result.status)
        verify(exactly = 0) { insumoRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando OS nao encontrada`() {
        every { jwt.getUsuarioId() } returns clienteId
        every { osRepository.buscarPorId(any()) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.reprovarOrcamento(OrdemServicoId.gerar().valor, ReprovarOrcamentoReqDTO("Motivo"))
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando solicitante nao e o cliente titular`() {
        val os = buildOs()
        val outroClienteId = UsuarioId.gerar()
        every { jwt.getUsuarioId() } returns outroClienteId
        every { osRepository.buscarPorId(any()) } returns os

        assertThrows<OperacaoNegadaException> {
            service.reprovarOrcamento(os.id.valor, ReprovarOrcamentoReqDTO("Motivo"))
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }
}
