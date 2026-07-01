package br.com.servicetrack.domain.notificacao.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class AssuntoNotificacaoTest {

    @Test
    fun `deve criar assunto quando valor for valido`() {
        val assunto = AssuntoNotificacao("OS atualizada")
        assertEquals("OS atualizada", assunto.valor)
    }

    @Test
    fun `deve criar assunto no limite maximo de caracteres`() {
        val valor = "a".repeat(AssuntoNotificacao.TAMANHO_MAXIMO)
        val assunto = AssuntoNotificacao(valor)
        assertEquals(valor, assunto.valor)
    }

    @Test
    fun `deve lancar excecao quando assunto for vazio`() {
        val ex = assertThrows<DomainException> { AssuntoNotificacao("") }
        assertEquals("Assunto da notificação não pode ser vazio", ex.message)
    }

    @Test
    fun `deve lancar excecao quando assunto for somente espacos`() {
        assertThrows<DomainException> { AssuntoNotificacao("   ") }
    }

    @Test
    fun `deve lancar excecao quando assunto exceder tamanho maximo`() {
        val valor = "a".repeat(AssuntoNotificacao.TAMANHO_MAXIMO + 1)
        val ex = assertThrows<DomainException> { AssuntoNotificacao(valor) }
        assertEquals(
            "Assunto da notificação deve ter no máximo ${AssuntoNotificacao.TAMANHO_MAXIMO} caracteres",
            ex.message,
        )
    }
}

