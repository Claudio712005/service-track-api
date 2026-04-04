package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SenhaTest {

    @Test
    fun `deve criar uma senha válida`() {
        val senha = Senha("Ab123@")

        assertEquals("Ab123@", senha.valor)
    }

    @Test
    fun `deve lançar exceção quando senha tiver menos de 6 caracteres`() {
        val exception = assertThrows<DomainException> {
            Senha("Ab1@")
        }

        assertEquals("A senha deve ter no mínimo 6 caracteres", exception.message)
    }

    @Test
    fun `deve lançar exceção quando não tiver letra maiúscula`() {
        val exception = assertThrows<DomainException> {
            Senha("ab123@")
        }

        assertEquals("A senha deve conter ao menos uma letra maiúscula", exception.message)
    }

    @Test
    fun `deve lançar exceção quando não tiver letra minúscula`() {
        val exception = assertThrows<DomainException> {
            Senha("AB123@")
        }

        assertEquals("A senha deve conter ao menos uma letra minúscula", exception.message)
    }

    @Test
    fun `deve lançar exceção quando não tiver número`() {
        val exception = assertThrows<DomainException> {
            Senha("Abcdef@")
        }

        assertEquals("A senha deve conter ao menos um número", exception.message)
    }

    @Test
    fun `deve lançar exceção quando não tiver caractere especial`() {
        val exception = assertThrows<DomainException> {
            Senha("Abc1234")
        }

        assertEquals("A senha deve conter ao menos um caractere especial", exception.message)
    }
}