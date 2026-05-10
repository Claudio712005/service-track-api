package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.ordemServico.ports.out.ItemOrdemServicoRepositoryPort
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.domain.ordemServico.ItemOrdemServico
import br.com.servicetrack.domain.ordemServico.vo.ItemOrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.UnidadeTempoEnum
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime

class BuscarTempoMedioConclusaoServiceTest {

    private val servicoRepository = mockk<ServicoRepositoryPort>()
    private val itemRepository = mockk<ItemOrdemServicoRepositoryPort>()

    private val service = BuscarTempoMedioConclusaoService(servicoRepository, itemRepository)

    private val servicoId = ServicoId.gerar()

    private fun buildServico(): Servico = Servico.reconstituir(
        id = servicoId,
        nomeServico = "Troca de Óleo",
        descricaoServico = "Substituição do óleo e filtro",
        valorReferencia = ValorMonetario(BigDecimal("150.00")),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
    )

    private fun buildItemConcluido(duracaoSegundos: Long): ItemOrdemServico {
        val criacao = LocalDateTime.now().minusSeconds(duracaoSegundos)
        val realizacao = LocalDateTime.now()
        return ItemOrdemServico.reconstituir(
            id = ItemOrdemServicoId.gerar(),
            servicoId = servicoId,
            ordemServicoId = OrdemServicoId.gerar(),
            valor = ValorMonetario(BigDecimal("150.00")),
            feito = true,
            mecanicoResponsavelId = null,
            dataRealizacao = realizacao,
            observacao = "Concluído",
            dataCriacao = criacao,
            dataAtualizacao = realizacao,
        )
    }

    @Test
    fun `deve retornar zero amostras quando nao ha itens concluidos`() {
        every { servicoRepository.buscarPorId(servicoId) } returns buildServico()
        every { itemRepository.buscarItensConcluidos(servicoId) } returns emptyList()

        val result = service.buscarTempoMedio(servicoId.valor, UnidadeTempoEnum.HORAS)

        assertEquals(0.0, result.tempoMedio)
        assertEquals(0, result.totalAmostras)
        assertEquals(UnidadeTempoEnum.HORAS, result.unidade)
        assertEquals(servicoId.valor, result.servicoId)
    }

    @Test
    fun `deve calcular tempo medio em segundos`() {
        every { servicoRepository.buscarPorId(servicoId) } returns buildServico()
        every { itemRepository.buscarItensConcluidos(servicoId) } returns listOf(
            buildItemConcluido(3600),
            buildItemConcluido(7200),
        )

        val result = service.buscarTempoMedio(servicoId.valor, UnidadeTempoEnum.SEGUNDOS)

        assertEquals(2, result.totalAmostras)
        assertEquals(5400.0, result.tempoMedio, 1.0)
        assertEquals(UnidadeTempoEnum.SEGUNDOS, result.unidade)
    }

    @Test
    fun `deve calcular tempo medio em minutos`() {
        every { servicoRepository.buscarPorId(servicoId) } returns buildServico()
        every { itemRepository.buscarItensConcluidos(servicoId) } returns listOf(
            buildItemConcluido(3600),
            buildItemConcluido(7200),
        )

        val result = service.buscarTempoMedio(servicoId.valor, UnidadeTempoEnum.MINUTOS)

        assertEquals(2, result.totalAmostras)
        assertEquals(90.0, result.tempoMedio, 1.0)
        assertEquals(UnidadeTempoEnum.MINUTOS, result.unidade)
    }

    @Test
    fun `deve calcular tempo medio em horas`() {
        every { servicoRepository.buscarPorId(servicoId) } returns buildServico()
        every { itemRepository.buscarItensConcluidos(servicoId) } returns listOf(
            buildItemConcluido(3600),
            buildItemConcluido(7200),
        )

        val result = service.buscarTempoMedio(servicoId.valor, UnidadeTempoEnum.HORAS)

        assertEquals(2, result.totalAmostras)
        assertEquals(1.5, result.tempoMedio, 0.01)
        assertEquals(UnidadeTempoEnum.HORAS, result.unidade)
    }

    @Test
    fun `deve calcular tempo medio em dias`() {
        every { servicoRepository.buscarPorId(servicoId) } returns buildServico()
        every { itemRepository.buscarItensConcluidos(servicoId) } returns listOf(
            buildItemConcluido(86400),
            buildItemConcluido(172800),
        )

        val result = service.buscarTempoMedio(servicoId.valor, UnidadeTempoEnum.DIAS)

        assertEquals(2, result.totalAmostras)
        assertEquals(1.5, result.tempoMedio, 0.01)
        assertEquals(UnidadeTempoEnum.DIAS, result.unidade)
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando servico nao encontrado`() {
        every { servicoRepository.buscarPorId(servicoId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.buscarTempoMedio(servicoId.valor, UnidadeTempoEnum.HORAS)
        }
    }
}
