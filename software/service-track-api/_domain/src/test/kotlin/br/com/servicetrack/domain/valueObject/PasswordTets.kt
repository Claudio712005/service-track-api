package br.com.servicetrack.domain.valueObject

import br.com.servicetrack.domain.exception.DomainException
import org.junit.Test
import kotlin.test.assertEquals

class PasswordTets {

    @Test
    fun `should create a valid password`() {
        val password = Password("Ab123@")

        assertEquals(password.value, "Ab123@")
    }

    @Test
    fun `should throw exception for password less than 6 characters`() {
        try {
            Password("Ab1@")
        } catch (e: DomainException) {
            assertEquals("Password must be at least 6 characters long", e.message)
        }
    }

    @Test
    fun `should throw exception for password without uppercase letter`() {
        try {
            Password("ab123@")
        } catch (e: DomainException) {
            assertEquals("Password must contain at least one uppercase letter", e.message)
        }
    }

    @Test
    fun `should throw exception for password without lowercase letter`() {
        try {
            Password("AB123@")
        } catch (e: DomainException) {
            assertEquals("Password must contain at least one lowercase letter", e.message)
        }
    }

    @Test
    fun `should throw exception for password without digit`() {
        try {
            Password("Abcdef@")
        } catch (e: DomainException) {
            assertEquals("Password must contain at least one digit", e.message)
        }
    }

    @Test
    fun `should throw exception for password without special character`() {
        try {
            Password("Abc1234")
        } catch (e: DomainException) {
            assertEquals("Password must contain at least one special character", e.message)
        }
    }
}