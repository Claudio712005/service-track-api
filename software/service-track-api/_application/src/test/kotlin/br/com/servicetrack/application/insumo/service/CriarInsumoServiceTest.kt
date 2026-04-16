package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.insumo.dto.CriarInsumoReqDTO
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CriarInsumoServiceTest {

    private val repository = mockk<InsumoRepositoryPort>()
    private val service = CriarInsumoService(repository)

    @Test
    fun `deve criar insumo com valores padrao de estoque`() {
        every { repository.salvar(any()) } returns Unit

        val req = CriarInsumoReqDTO(
            nome = "Filtro de Óleo",
            descricao = "Filtro de óleo para motor 1.0",
            custo = BigDecimal("25.50"),
            qtdEstoqueInicial = 0,
            estoqueMinimo = 0
        )

        val result = service.criarInsumo(req)

        assertNotNull(result.id)
        assertEquals("Filtro de Óleo", result.nome)
        assertEquals("Filtro de óleo para motor 1.0", result.descricao)
        assertEquals(BigDecimal("25.50"), result.custo)
        assertEquals(0, result.qtdEstoque)
        verify(exactly = 1) { repository.salvar(any()) }
    }

    @Test
    fun `deve criar insumo com estoque inicial e minimo definidos`() {
        every { repository.salvar(any()) } returns Unit

        val req = CriarInsumoReqDTO(
            nome = "Pastilha de Freio",
            descricao = "Pastilha dianteira para veículos compactos",
            custo = BigDecimal("89.90"),
            qtdEstoqueInicial = 10,
            estoqueMinimo = 2
        )

        val result = service.criarInsumo(req)

        assertNotNull(result.id)
        assertEquals("Pastilha de Freio", result.nome)
        assertEquals(10, result.qtdEstoque)
        assertEquals(2, result.estoqueMinimo)
        verify(exactly = 1) { repository.salvar(any()) }
    }
}
