package br.com.servicetrack.domain.insumo.vo

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InsumoIdTest {

    @Test
    fun `deve criar InsumoId a partir de valor existente`() {
        val valor = "550e8400-e29b-41d4-a716-446655440000"
        val id = InsumoId.de(valor)
        assertEquals(valor, id.valor)
    }
}