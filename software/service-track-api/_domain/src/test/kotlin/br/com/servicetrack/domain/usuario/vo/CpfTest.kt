package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.Test
import kotlin.test.assertEquals

class CpfTest {

    @Test
    fun `deve criar um cpf quando cpf for válido`() {
        val cpf = Cpf("549.271.700-63")
        assertEquals("549.271.700-63", cpf.valor)
    }

    @Test(expected = DomainException::class)
    fun `deve lançar excessão quando o cpf for vazio`() {
        Cpf("")
    }
}