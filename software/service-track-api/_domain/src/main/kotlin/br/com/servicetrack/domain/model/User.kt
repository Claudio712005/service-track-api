package br.com.servicetrack.domain.model

import br.com.servicetrack.domain.enums.UserRole
import br.com.servicetrack.domain.exception.DomainException
import br.com.servicetrack.domain.valueObject.Cpf
import br.com.servicetrack.domain.valueObject.Email
import br.com.servicetrack.domain.valueObject.Password
import br.com.servicetrack.domain.valueObject.PhoneNumber
import java.time.LocalDate
import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val name: String,
    val email: Email,
    val password: Password,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val dateOfBirth: LocalDate,
    val phoneNumber: PhoneNumber,
    val cpf: Cpf,
    val enabled: Boolean = true,
    val role: UserRole
) {

    init {
        if(name.isBlank()) {
            throw DomainException("Name cannot be blank")
        }
    }

    fun disable(): User = copy(enabled = false)

    fun enable(): User = copy(enabled = true)

    fun changePassword(newPassword: Password): User =
        copy(password = newPassword, updatedAt = LocalDateTime.now())
}