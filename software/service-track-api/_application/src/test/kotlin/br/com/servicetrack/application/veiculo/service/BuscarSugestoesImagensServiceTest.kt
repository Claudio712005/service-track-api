package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.veiculo.ports.out.UnsplashPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuscarSugestoesImagensServiceTest {

    private val unsplash = mockk<UnsplashPort>()
    private val service = BuscarSugestoesImagensService(unsplash)

    @Test
    fun `deve retornar sugestoes de imagens para marca e modelo informados`() {
        val urlsEsperadas = listOf(
            "https://images.unsplash.com/photo-abc",
            "https://images.unsplash.com/photo-def"
        )
        every { unsplash.buscarImagensVeiculo("Toyota", "Corolla", 10) } returns urlsEsperadas

        val resultado = service.buscarSugestoes("Toyota", "Corolla")

        assertEquals(2, resultado.imagens.size)
        assertEquals(urlsEsperadas, resultado.imagens)
        verify(exactly = 1) { unsplash.buscarImagensVeiculo("Toyota", "Corolla", 10) }
    }

    @Test
    fun `deve retornar lista vazia quando unsplash nao encontra imagens`() {
        every { unsplash.buscarImagensVeiculo("Honda", "Civic", 10) } returns emptyList()

        val resultado = service.buscarSugestoes("Honda", "Civic")

        assertTrue(resultado.imagens.isEmpty())
    }

    @Test
    fun `deve remover espacos extras de marca e modelo antes de buscar`() {
        val urls = listOf("https://images.unsplash.com/photo-xyz")
        every { unsplash.buscarImagensVeiculo("Fiat", "Uno", 10) } returns urls

        val resultado = service.buscarSugestoes("  Fiat  ", "  Uno  ")

        assertEquals(1, resultado.imagens.size)
        verify(exactly = 1) { unsplash.buscarImagensVeiculo("Fiat", "Uno", 10) }
    }
}
