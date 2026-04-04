package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class EmailTest {

    @Test
    fun `deve criar e-mail quando o e-mail for válido`() {
        val email = Email("test@email.com")
        assertEquals("test@email.com", email.valor)
    }

    @Test
    fun `deve lançar exceção quando o e-mail estiver em branco`() {
        assertThrows<DomainException> {
            Email("")
        }
    }

    @Test
    fun `deve lançar exceção quando o e-mail for inválido`() {
        assertThrows<DomainException> {
            Email("email-sem-arroba")
        }
    }
}
