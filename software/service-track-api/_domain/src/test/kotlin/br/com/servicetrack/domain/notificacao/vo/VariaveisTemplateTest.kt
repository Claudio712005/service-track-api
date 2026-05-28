package br.com.servicetrack.domain.notificacao.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VariaveisTemplateTest {

    @Test
    fun `deve criar a partir de map valido`() {
        val variaveis = VariaveisTemplate.de(mapOf("nome" to "Cláudio", "os" to "123"))

        assertEquals("Cláudio", variaveis["nome"])
        assertEquals("123", variaveis["os"])
        assertFalse(variaveis.estaVazio())
    }

    @Test
    fun `deve expor VAZIO como instancia sem chaves`() {
        assertTrue(VariaveisTemplate.VAZIO.estaVazio())
        assertEquals(emptyMap(), VariaveisTemplate.VAZIO.comoMap())
    }

    @Test
    fun `deve retornar null para chave inexistente`() {
        val variaveis = VariaveisTemplate.de(mapOf("a" to "1"))
        assertNull(variaveis["inexistente"])
    }

    @Test
    fun `deve realizar copia defensiva do map de entrada`() {
        val origem = mutableMapOf("a" to "1")
        val variaveis = VariaveisTemplate.de(origem)

        origem["b"] = "2"

        assertNull(variaveis["b"])
        assertEquals(1, variaveis.comoMap().size)
    }

    @Test
    fun `deve lancar excecao quando houver chave em branco`() {
        val ex = assertThrows<DomainException> {
            VariaveisTemplate.de(mapOf(" " to "valor"))
        }
        assertEquals("Chave de variável de template não pode ser vazia", ex.message)
    }

    @Test
    fun `deve lancar excecao quando houver chave vazia`() {
        assertThrows<DomainException> {
            VariaveisTemplate.de(mapOf("" to "valor"))
        }
    }

    @Test
    fun `deve comparar por igualdade de valores`() {
        val a = VariaveisTemplate.de(mapOf("k" to "v"))
        val b = VariaveisTemplate.de(mapOf("k" to "v"))
        val c = VariaveisTemplate.de(mapOf("k" to "outro"))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }
}

