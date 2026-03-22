package br.com.servicetrack.domain.model

import br.com.servicetrack.domain.enums.UserRole
import br.com.servicetrack.domain.exception.DomainException
import br.com.servicetrack.domain.valueObject.Cpf
import br.com.servicetrack.domain.valueObject.Email
import br.com.servicetrack.domain.valueObject.Password
import br.com.servicetrack.domain.valueObject.PhoneNumber
import org.junit.jupiter.api.Assertions.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class UserTest {

    private fun buildUser(): User {
        val now = LocalDateTime.now()

        return User(
            name = "Cláudio",
            email = Email("test@email.com"),
            password = Password("Strong@123"),
            createdAt = now,
            updatedAt = now,
            dateOfBirth = LocalDate.of(2000, 1, 1),
            phoneNumber = PhoneNumber("11999999999"),
            cpf = Cpf("12345678901"),
            role = UserRole.CUSTOMER
        )
    }

    @Test
    fun `should create valid user`() {
        val user = buildUser()

        assertEquals("Cláudio", user.name)
        assertTrue(user.enabled)
    }

    @Test(expected = DomainException::class)
    fun `should throw exception when name is blank`() {
        val now = LocalDateTime.now()

        User(
            name = "",
            email = Email("test@email.com"),
            password = Password("Strong@123"),
            createdAt = now,
            updatedAt = now,
            dateOfBirth = LocalDate.of(2000, 1, 1),
            phoneNumber = PhoneNumber("11999999999"),
            cpf = Cpf("12345678901"),
            role = UserRole.CUSTOMER
        )

    }

    @Test
    fun `should disable user`() {
        val user = buildUser()

        val disabledUser = user.disable()

        assertFalse(disabledUser.enabled)
        assertTrue(user.enabled)
    }

    @Test
    fun `should enable user`() {
        val user = buildUser().disable()

        val enabledUser = user.enable()

        assertTrue(enabledUser.enabled)
        assertFalse(user.enabled)
    }

    @Test
    fun `should change password and update timestamp`() {
        val user = buildUser()
        val newPassword = Password("NewStrong@123")

        val updatedUser = user.changePassword(newPassword)

        assertEquals(newPassword, updatedUser.password)
        assertTrue(updatedUser.updatedAt.isAfter(user.updatedAt))
    }

    @Test
    fun `should keep immutability when changing password`() {
        val user = buildUser()
        val newPassword = Password("NewStrong@123")

        val updatedUser = user.changePassword(newPassword)

        assertNotSame(user, updatedUser)
        assertNotEquals(user.password, updatedUser.password)
    }
}