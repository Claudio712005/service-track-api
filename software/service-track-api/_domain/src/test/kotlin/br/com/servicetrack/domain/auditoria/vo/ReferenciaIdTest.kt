package br.com.servicetrack.domain.auditoria.vo

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class ReferenciaIdTest {

    @Test
    fun `deve gerar ReferenciaId com valor não nulo`() {
        val id = ReferenciaId.gerar()
        assertNotNull(id.value)
    }

    @Test
    fun `deve gerar ReferenciaId no formato UUID`() {
        val id = ReferenciaId.gerar()
        val uuidRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
        assert(uuidRegex.matches(id.value))
    }

    @Test
    fun `deve criar ReferenciaId a partir de valor existente`() {
        val valor = "550e8400-e29b-41d4-a716-446655440000"
        val id = ReferenciaId(valor)
        assertEquals(valor, id.value)
    }

    @Test
    fun `deve gerar IDs únicos a cada chamada`() {
        val id1 = ReferenciaId.gerar()
        val id2 = ReferenciaId.gerar()
        assertNotEquals(id1.value, id2.value)
    }
}
