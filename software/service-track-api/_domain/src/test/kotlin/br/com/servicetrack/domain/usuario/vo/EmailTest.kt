package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.Test
import kotlin.test.assertEquals

class EmailTest{

    @Test
    fun `deve criar e-mail quando o e-mail for válido`() {
        val email = Email("test@email.com")
        assertEquals("test@email.com", email.valor)
    }

    @Test(expected = DomainException::class)
    fun `deve lançar excessão quando o e-mail estiver em branco`() {
        Email("")
    }
}