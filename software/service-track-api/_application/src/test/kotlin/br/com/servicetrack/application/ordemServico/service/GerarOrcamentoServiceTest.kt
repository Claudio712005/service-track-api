package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.ordemServico.ItemOrdemServico
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.ItemOrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.exception.DomainException
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

class GerarOrcamentoServiceTest {

    private val osRepository = mockk<OrdemServicoRepositoryPort>()
    private val insumoRepository = mockk<InsumoRepositoryPort>()
    private val jwt = mockk<JwtPort>()

    private val service = GerarOrcamentoService(osRepository, insumoRepository, jwt)

    private val mecanicoId = UsuarioId.gerar()
    private val clienteId = UsuarioId.gerar()

    private fun buildInsumo(id: InsumoId = InsumoId.gerar(), qtd: Int = 10): Insumo = Insumo.reconstituir(
        id = id,
        nome = "Filtro de Óleo",
        descricao = "Filtro motor 1.0",
        custo = ValorMonetario(BigDecimal("25.00")),
        estoqueMinimo = 2,
        qtdEstoque = qtd,
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
    )

    private fun buildItemServico(osId: OrdemServicoId): ItemOrdemServico {
        val agora = LocalDateTime.now()
        return ItemOrdemServico.reconstituir(
            id = ItemOrdemServicoId.gerar(),
            servicoId = ServicoId.gerar(),
            ordemServicoId = osId,
            valor = ValorMonetario(BigDecimal("120.00")),
            feito = false,
            mecanicoResponsavelId = null,
            dataRealizacao = null,
            observacao = null,
            dataCriacao = agora,
            dataAtualizacao = agora,
        )
    }

    private fun buildOs(
        status: StatusOrdemServicoEnum = StatusOrdemServicoEnum.EM_DIAGNOSTICO,
        mecanicoId: UsuarioId = this.mecanicoId,
        insumos: List<InsumoId> = emptyList(),
        itens: List<ItemOrdemServico> = emptyList(),
    ): OrdemServico {
        val osId = OrdemServicoId.gerar()
        return OrdemServico.reconstituir(
            id = osId,
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
            insumos = insumos.toMutableList(),
            itensServico = itens.toMutableList(),
        )
    }

    @Test
    fun `deve gerar orcamento quando OS esta em diagnostico com servicos e insumos`() {
        val insumoId = InsumoId.gerar()
        val insumo = buildInsumo(insumoId)
        val osId = OrdemServicoId.gerar()
        val item = buildItemServico(osId)
        val os = OrdemServico.reconstituir(
            id = osId,
            motivo = "Revisão geral",
            observacao = "",
            clienteId = clienteId,
            mecanicoId = mecanicoId,
            veiculoId = VeiculoId.gerar(),
            dataCriacao = LocalDateTime.now(),
            dataAtualizacao = LocalDateTime.now(),
            status = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.EM_DIAGNOSTICO),
            prazoConclusao = null,
            orcamento = null,
            insumos = mutableListOf(insumoId),
            itensServico = mutableListOf(item),
        )
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os
        every { insumoRepository.buscarPorId(insumoId) } returns insumo
        justRun { insumoRepository.atualizar(any()) }
        every { osRepository.atualizar(any()) } answers { firstArg() }

        val result = service.gerarOrcamento(os.id.valor)

        assertEquals(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO, result.status)
        verify(exactly = 1) { insumoRepository.atualizar(insumo) }
        verify(exactly = 1) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando OS nao encontrada`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.gerarOrcamento(OrdemServicoId.gerar().valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando solicitante nao e o mecanico vinculado`() {
        val os = buildOs()
        val outroMecanicoId = UsuarioId.gerar()
        every { jwt.getUsuarioId() } returns outroMecanicoId
        every { osRepository.buscarPorId(any()) } returns os

        assertThrows<OperacaoNegadaException> {
            service.gerarOrcamento(os.id.valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando OS nao esta em diagnostico`() {
        val os = buildOs(status = StatusOrdemServicoEnum.RECEBIDA)
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os

        assertThrows<OperacaoNegadaException> {
            service.gerarOrcamento(os.id.valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando OS nao tem servicos associados`() {
        val insumoId = InsumoId.gerar()
        val os = buildOs(insumos = listOf(insumoId))
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os

        assertThrows<DomainException> {
            service.gerarOrcamento(os.id.valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando OS nao tem insumos associados`() {
        val osId = OrdemServicoId.gerar()
        val item = buildItemServico(osId)
        val os = OrdemServico.reconstituir(
            id = osId,
            motivo = "Revisão geral",
            observacao = "",
            clienteId = clienteId,
            mecanicoId = mecanicoId,
            veiculoId = VeiculoId.gerar(),
            dataCriacao = LocalDateTime.now(),
            dataAtualizacao = LocalDateTime.now(),
            status = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.EM_DIAGNOSTICO),
            prazoConclusao = null,
            orcamento = null,
            insumos = mutableListOf(),
            itensServico = mutableListOf(item),
        )
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os

        assertThrows<DomainException> {
            service.gerarOrcamento(os.id.valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }
}
