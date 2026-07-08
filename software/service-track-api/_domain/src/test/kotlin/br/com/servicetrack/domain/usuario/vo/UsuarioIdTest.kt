package br.com.servicetrack.domain.usuario.vo

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class UsuarioIdTest {

    @Test
    fun `deve criar UsuarioId com valor fornecido`() {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        val id = UsuarioId(uuid)
        assertEquals(uuid, id.valor)
    }

    @Test
    fun `gerar deve retornar UsuarioId com UUID válido`() {
        val id = UsuarioId.gerar()
        assertNotNull(id.valor)
        val uuidRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
        assert(uuidRegex.matches(id.valor)) { "Esperava UUID válido, mas recebeu: ${id.valor}" }
    }

    @Test
    fun `gerar deve retornar ids únicos a cada chamada`() {
        val id1 = UsuarioId.gerar()
        val id2 = UsuarioId.gerar()
        assertNotEquals(id1, id2)
    }
}
