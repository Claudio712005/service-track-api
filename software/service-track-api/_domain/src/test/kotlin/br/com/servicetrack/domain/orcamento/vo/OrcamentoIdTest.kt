package br.com.servicetrack.domain.orcamento.vo

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID
import kotlin.test.assertEquals

class OrcamentoIdTest {

    @Test
    fun `deve criar OrcamentoId a partir de valor existente`() {
        val valor = "550e8400-e29b-41d4-a716-446655440000"
        val id = OrcamentoId.de(valor)
        assertEquals(valor, id.valor)
    }

    @Test
    fun `deve gerar OrcamentoId com UUID válido`(){
        val id = OrcamentoId.gerar()
        assertDoesNotThrow {
            UUID.fromString(id.valor)
        }
    }
}