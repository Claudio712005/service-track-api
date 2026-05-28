package br.com.servicetrack.domain.notificacao.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class TituloNotificacaoTest {

    @Test
    fun `deve criar titulo quando valor for valido`() {
        val titulo = TituloNotificacao("Atualização da OS")
        assertEquals("Atualização da OS", titulo.valor)
    }

    @Test
    fun `deve criar titulo no limite maximo de caracteres`() {
        val valor = "a".repeat(TituloNotificacao.TAMANHO_MAXIMO)
        val titulo = TituloNotificacao(valor)
        assertEquals(valor, titulo.valor)
    }

    @Test
    fun `deve lancar excecao quando titulo for vazio`() {
        val ex = assertThrows<DomainException> { TituloNotificacao("") }
        assertEquals("Título da notificação não pode ser vazio", ex.message)
    }

    @Test
    fun `deve lancar excecao quando titulo for somente espacos`() {
        assertThrows<DomainException> { TituloNotificacao("   ") }
    }

    @Test
    fun `deve lancar excecao quando titulo exceder tamanho maximo`() {
        val valor = "a".repeat(TituloNotificacao.TAMANHO_MAXIMO + 1)
        val ex = assertThrows<DomainException> { TituloNotificacao(valor) }
        assertEquals(
            "Título da notificação deve ter no máximo ${TituloNotificacao.TAMANHO_MAXIMO} caracteres",
            ex.message,
        )
    }
}

