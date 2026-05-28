package br.com.servicetrack.domain.notificacao.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class DescricaoNotificacaoTest {

    @Test
    fun `deve criar descricao quando valor for valido`() {
        val descricao = DescricaoNotificacao("Sua ordem de serviço foi atualizada.")
        assertEquals("Sua ordem de serviço foi atualizada.", descricao.valor)
    }

    @Test
    fun `deve criar descricao no limite maximo de caracteres`() {
        val valor = "a".repeat(DescricaoNotificacao.TAMANHO_MAXIMO)
        val descricao = DescricaoNotificacao(valor)
        assertEquals(valor, descricao.valor)
    }

    @Test
    fun `deve lancar excecao quando descricao for vazia`() {
        val ex = assertThrows<DomainException> { DescricaoNotificacao("") }
        assertEquals("Descrição da notificação não pode ser vazia", ex.message)
    }

    @Test
    fun `deve lancar excecao quando descricao for somente espacos`() {
        assertThrows<DomainException> { DescricaoNotificacao("    ") }
    }

    @Test
    fun `deve lancar excecao quando descricao exceder tamanho maximo`() {
        val valor = "a".repeat(DescricaoNotificacao.TAMANHO_MAXIMO + 1)
        val ex = assertThrows<DomainException> { DescricaoNotificacao(valor) }
        assertEquals(
            "Descrição da notificação deve ter no máximo ${DescricaoNotificacao.TAMANHO_MAXIMO} caracteres",
            ex.message,
        )
    }
}

