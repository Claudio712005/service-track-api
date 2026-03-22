package br.com.servicetrack.domain.valueObject

import br.com.servicetrack.domain.exception.DomainException
import org.junit.Test
import kotlin.test.assertEquals

class EmailTest{

    @Test
    fun `should create a valid email`() {
        val email = Email("test@email.com")
        assertEquals("test@email.com", email.value)
    }

    @Test(expected = DomainException::class)
    fun `should throw exception for blank email`() {
        Email("")
    }
}