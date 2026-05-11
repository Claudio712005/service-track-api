package br.com.servicetrack.domain.shared.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ImagemUrlTest {

    @Test
    fun `deve criar ImagemUrl valida com http`() {
        val imagemUrl = ImagemUrl.criar("http://exemplo.com/foto.jpg")
        assertEquals("http://exemplo.com/foto.jpg", imagemUrl.url)
    }

    @Test
    fun `deve criar ImagemUrl valida com https`() {
        val imagemUrl = ImagemUrl.criar("https://images.unsplash.com/photo-abc123")
        assertEquals("https://images.unsplash.com/photo-abc123", imagemUrl.url)
    }

    @Test
    fun `deve lançar exceção quando url é vazia`() {
        val exception = assertThrows<DomainException> {
            ImagemUrl.criar("")
        }
        assertEquals("URL da imagem não pode ser vazia", exception.message)
    }

    @Test
    fun `deve lançar exceção quando url é apenas espaços`() {
        val exception = assertThrows<DomainException> {
            ImagemUrl.criar("   ")
        }
        assertEquals("URL da imagem não pode ser vazia", exception.message)
    }

    @Test
    fun `deve lançar exceção quando url não começa com http ou https`() {
        val exception = assertThrows<DomainException> {
            ImagemUrl.criar("ftp://exemplo.com/foto.jpg")
        }
        assertEquals("URL da imagem deve começar com http:// ou https://", exception.message)
    }

    @Test
    fun `deve lançar exceção quando url é sem protocolo`() {
        val exception = assertThrows<DomainException> {
            ImagemUrl.criar("www.exemplo.com/foto.jpg")
        }
        assertEquals("URL da imagem deve começar com http:// ou https://", exception.message)
    }
}
