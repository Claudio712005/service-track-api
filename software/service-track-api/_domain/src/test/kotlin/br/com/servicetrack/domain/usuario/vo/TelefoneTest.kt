package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class TelefoneTest {

    @Test
    fun `deve criar telefone válido com 11 dígitos`() {
        val telefone = Telefone("11987654321")

        assertEquals("11987654321", telefone.valor)
    }

    @Test
    fun `deve criar telefone válido com 10 dígitos`() {
        val telefone = Telefone("1132654321")

        assertEquals("1132654321", telefone.valor)
    }

    @Test
    fun `deve lançar exceção quando telefone for vazio`() {
        val exception = assertThrows<DomainException> {
            Telefone("")
        }

        assertEquals("Telefone não pode ser vazio", exception.message)
    }

    @Test
    fun `deve lançar exceção quando telefone tiver menos de 10 dígitos`() {
        val exception = assertThrows<DomainException> {
            Telefone("119876543")
        }

        assertEquals("Telefone deve conter entre 10 e 11 dígitos", exception.message)
    }

    @Test
    fun `deve lançar exceção quando telefone tiver mais de 11 dígitos`() {
        val exception = assertThrows<DomainException> {
            Telefone("119876543210")
        }

        assertEquals("Telefone deve conter entre 10 e 11 dígitos", exception.message)
    }

    @Test
    fun `deve lançar exceção quando telefone conter letras`() {
        val exception = assertThrows<DomainException> {
            Telefone("11A87654321")
        }

        assertEquals("Telefone deve conter apenas números", exception.message)
    }

    @Test
    fun `deve lançar exceção quando telefone conter caracteres especiais`() {
        val exception = assertThrows<DomainException> {
            Telefone("(11)98765-4321")
        }

        assertEquals("Telefone deve conter apenas números", exception.message)
    }
}