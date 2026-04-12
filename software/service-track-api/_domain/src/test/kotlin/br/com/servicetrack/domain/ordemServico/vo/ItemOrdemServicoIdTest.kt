package br.com.servicetrack.domain.ordemServico.vo

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import kotlin.test.assertEquals

class ItemOrdemServicoIdTest {

    @Test
    fun `deve criar ItemOrdemServicoId a partir de valor existente`() {
        val valor = "550e8400-e29b-41d4-a716-446655440000"
        val id = ItemOrdemServicoId.de(valor)
        assertEquals(valor, id.valor)
    }

    @Test
    fun `deve gerar ItemOrdemServicoId com UUID válido`(){
        val id = ItemOrdemServicoId.gerar()
        assertDoesNotThrow {
            UUID.fromString(id.valor)
        }
    }
}
