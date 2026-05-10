package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.application.ordemServico.dto.request.AssociarItensReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.ItemInsumoReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.ItemServicoReqDTO
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.exception.DomainException
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

class AssociarItensOrdemServicoServiceTest {

    private val osRepository = mockk<OrdemServicoRepositoryPort>()
    private val servicoRepository = mockk<ServicoRepositoryPort>()
    private val insumoRepository = mockk<InsumoRepositoryPort>()
    private val jwt = mockk<JwtPort>()

    private val service = AssociarItensOrdemServicoService(osRepository, servicoRepository, insumoRepository, jwt)

    private val mecanicoId = UsuarioId.gerar()
    private val clienteId = UsuarioId.gerar()

    private fun buildServico(id: ServicoId = ServicoId.gerar()): Servico = Servico.reconstituir(
        id = id,
        nomeServico = "Troca de Óleo",
        descricaoServico = "Substituição do óleo do motor",
        valorReferencia = ValorMonetario(BigDecimal("150.00")),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
    )

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

    private fun buildOs(
        status: StatusOrdemServicoEnum = StatusOrdemServicoEnum.EM_DIAGNOSTICO,
        mecanicoId: UsuarioId = this.mecanicoId,
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
        orcamento = null,
        insumos = mutableListOf(),
        itensServico = mutableListOf(),
    )

    @Test
    fun `deve associar servicos e insumos quando OS em diagnostico e mecanico correto`() {
        val servicoId = ServicoId.gerar()
        val insumoId = InsumoId.gerar()
        val servico = buildServico(servicoId)
        val insumo = buildInsumo(insumoId)
        val os = buildOs()
        val req = AssociarItensReqDTO(
            servicos = listOf(ItemServicoReqDTO(servicoId.valor, null)),
            insumos = listOf(ItemInsumoReqDTO(insumoId.valor, 2)),
        )
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os
        every { servicoRepository.buscarPorId(servicoId) } returns servico
        every { insumoRepository.buscarPorId(insumoId) } returns insumo
        every { osRepository.atualizar(any()) } answers { firstArg() }

        val result = service.associarItens(os.id.valor, req)

        assertEquals(1, result.itensServico.size)
        verify(exactly = 1) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve usar valorCobrado quando informado em vez do valor de referencia`() {
        val servicoId = ServicoId.gerar()
        val insumoId = InsumoId.gerar()
        val servico = buildServico(servicoId)
        val insumo = buildInsumo(insumoId)
        val os = buildOs()
        val valorCustomizado = BigDecimal("200.00")
        val req = AssociarItensReqDTO(
            servicos = listOf(ItemServicoReqDTO(servicoId.valor, valorCustomizado)),
            insumos = listOf(ItemInsumoReqDTO(insumoId.valor, 1)),
        )
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os
        every { servicoRepository.buscarPorId(servicoId) } returns servico
        every { insumoRepository.buscarPorId(insumoId) } returns insumo
        every { osRepository.atualizar(any()) } answers { firstArg() }

        val result = service.associarItens(os.id.valor, req)

        assertEquals(valorCustomizado, result.itensServico.first().valor)
        verify(exactly = 1) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando OS nao encontrada`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.associarItens(
                OrdemServicoId.gerar().valor,
                AssociarItensReqDTO(
                    listOf(ItemServicoReqDTO(ServicoId.gerar().valor, null)),
                    listOf(ItemInsumoReqDTO(InsumoId.gerar().valor, 1)),
                ),
            )
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
            service.associarItens(
                os.id.valor,
                AssociarItensReqDTO(
                    listOf(ItemServicoReqDTO(ServicoId.gerar().valor, null)),
                    listOf(ItemInsumoReqDTO(InsumoId.gerar().valor, 1)),
                ),
            )
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando OS nao esta em diagnostico`() {
        val os = buildOs(status = StatusOrdemServicoEnum.RECEBIDA)
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os

        assertThrows<OperacaoNegadaException> {
            service.associarItens(
                os.id.valor,
                AssociarItensReqDTO(
                    listOf(ItemServicoReqDTO(ServicoId.gerar().valor, null)),
                    listOf(ItemInsumoReqDTO(InsumoId.gerar().valor, 1)),
                ),
            )
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando lista de servicos esta vazia`() {
        val os = buildOs()
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os

        assertThrows<DomainException> {
            service.associarItens(
                os.id.valor,
                AssociarItensReqDTO(
                    servicos = emptyList(),
                    insumos = listOf(ItemInsumoReqDTO(InsumoId.gerar().valor, 1)),
                ),
            )
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando servico nao encontrado`() {
        val servicoId = ServicoId.gerar()
        val insumoId = InsumoId.gerar()
        val os = buildOs()
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os
        every { servicoRepository.buscarPorId(servicoId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.associarItens(
                os.id.valor,
                AssociarItensReqDTO(
                    listOf(ItemServicoReqDTO(servicoId.valor, null)),
                    listOf(ItemInsumoReqDTO(insumoId.valor, 1)),
                ),
            )
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando estoque insuficiente para o insumo`() {
        val servicoId = ServicoId.gerar()
        val insumoId = InsumoId.gerar()
        val servico = buildServico(servicoId)
        val insumoSemEstoque = buildInsumo(insumoId, qtd = 1)
        val os = buildOs()
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os
        every { servicoRepository.buscarPorId(servicoId) } returns servico
        every { insumoRepository.buscarPorId(insumoId) } returns insumoSemEstoque

        assertThrows<DomainException> {
            service.associarItens(
                os.id.valor,
                AssociarItensReqDTO(
                    listOf(ItemServicoReqDTO(servicoId.valor, null)),
                    listOf(ItemInsumoReqDTO(insumoId.valor, 5)),
                ),
            )
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }
}
