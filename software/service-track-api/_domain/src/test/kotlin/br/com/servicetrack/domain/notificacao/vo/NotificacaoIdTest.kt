package br.com.servicetrack.domain.notificacao.vo

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class NotificacaoIdTest {

    @Test
    fun `deve gerar id com valor nao nulo`() {
        val id = NotificacaoId.gerar()
        assertNotNull(id.value)
    }

    @Test
    fun `deve gerar ids diferentes a cada chamada`() {
        val a = NotificacaoId.gerar()
        val b = NotificacaoId.gerar()
        assertNotEquals(a.value, b.value)
    }

    @Test
    fun `deve criar id a partir de UUID`() {
        val uuid = UUID.randomUUID()
        val id = NotificacaoId.de(uuid)
        assertEquals(uuid.toString(), id.value)
    }
}

