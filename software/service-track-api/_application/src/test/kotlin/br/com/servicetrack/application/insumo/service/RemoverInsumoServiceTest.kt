package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class RemoverInsumoServiceTest {

    private val repository = mockk<InsumoRepositoryPort>()
    private val service = RemoverInsumoService(repository)

    @Test
    fun `deve remover insumo existente com sucesso`() {
        val insumo = Insumo.criar(
            nome = "Filtro de Óleo",
            descricao = "Filtro motor 1.0",
            custo = ValorMonetario(BigDecimal("25.50")),
            qtdEstoqueInicial = 10,
            estoqueMinimo = 2
        )
        every { repository.buscarPorId(insumo.id) } returns insumo
        every { repository.remover(insumo.id) } returns Unit

        service.removerInsumo(insumo.id)

        verify(exactly = 1) { repository.buscarPorId(insumo.id) }
        verify(exactly = 1) { repository.remover(insumo.id) }
    }

    @Test
    fun `deve remover insumo com estoque zerado`() {
        val insumo = Insumo.criar(
            nome = "Pastilha de Freio",
            descricao = "Pastilha dianteira",
            custo = ValorMonetario(BigDecimal("89.90")),
            qtdEstoqueInicial = 0,
            estoqueMinimo = 0
        )
        every { repository.buscarPorId(insumo.id) } returns insumo
        every { repository.remover(insumo.id) } returns Unit

        service.removerInsumo(insumo.id)

        verify(exactly = 1) { repository.remover(insumo.id) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando insumo nao encontrado`() {
        val id = InsumoId.gerar()
        every { repository.buscarPorId(id) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.removerInsumo(id)
        }

        verify(exactly = 1) { repository.buscarPorId(id) }
        verify(exactly = 0) { repository.remover(any()) }
    }
}
