package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SenhaTest {

    @Test
    fun `deve criar uma senha válida`() {
        val senha = Senha.criar("Ab123@")
        assertEquals("Ab123@", senha.valor)
    }

    @Test
    fun `deve lançar exceção quando senha tiver menos de 6 caracteres`() {
        val exception = assertThrows<DomainException> {
            Senha.criar("Ab1@")
        }
        assertEquals("A senha deve ter no mínimo 6 caracteres", exception.message)
    }

    @Test
    fun `deve lançar exceção quando não tiver letra maiúscula`() {
        val exception = assertThrows<DomainException> {
            Senha.criar("ab123@")
        }
        assertEquals("A senha deve conter ao menos uma letra maiúscula", exception.message)
    }

    @Test
    fun `deve lançar exceção quando não tiver letra minúscula`() {
        val exception = assertThrows<DomainException> {
            Senha.criar("AB123@")
        }
        assertEquals("A senha deve conter ao menos uma letra minúscula", exception.message)
    }

    @Test
    fun `deve lançar exceção quando não tiver número`() {
        val exception = assertThrows<DomainException> {
            Senha.criar("Abcdef@")
        }
        assertEquals("A senha deve conter ao menos um número", exception.message)
    }

    @Test
    fun `deve lançar exceção quando não tiver caractere especial`() {
        val exception = assertThrows<DomainException> {
            Senha.criar("Abc1234")
        }
        assertEquals("A senha deve conter ao menos um caractere especial", exception.message)
    }

    @Test
    fun `deve criar senha a partir de hash sem validar política`() {
        val hash = "\$2a\$10\$xyzAbcDef1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgh"
        val senha = Senha.deHash(hash)
        assertEquals(hash, senha.valor)
    }

    @Test
    fun `deve lançar exceção ao criar hash vazio`() {
        assertThrows<IllegalArgumentException> {
            Senha.deHash("")
        }
    }
}
