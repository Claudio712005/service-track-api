package br.com.servicetrack.domain.valueObject

import br.com.servicetrack.domain.exception.DomainException
import org.junit.Test
import kotlin.test.assertEquals

class CpfTest {

    @Test
    fun `should create a valid cpf`() {
        val cpf = Cpf("12345678901")
        assertEquals(cpf.value, "12345678901")
    }

    @Test(expected = DomainException::class)
    fun `should throw exception for blank cpf`() {
        Cpf("")
    }
}