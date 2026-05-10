package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CpfTest {

    @Test
    fun `deve criar um cpf quando cpf for válido`() {
        val cpf = Cpf("549.271.700-63")
        assertEquals("549.271.700-63", cpf.valor)
    }

    @Test
    fun `deve criar cpf com apenas dígitos`() {
        val cpf = Cpf("54927170063")
        assertEquals("54927170063", cpf.valor)
    }

    @Test
    fun `deve lançar exceção quando o cpf for vazio`() {
        assertThrows<DomainException> {
            Cpf("")
        }
    }

    @Test
    fun `deve lançar exceção quando cpf tiver dígitos repetidos`() {
        assertThrows<DomainException> {
            Cpf("11111111111")
        }
    }

    @Test
    fun `deve lançar exceção quando cpf for inválido`() {
        assertThrows<DomainException> {
            Cpf("12345678900")
        }
    }
}
