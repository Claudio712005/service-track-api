package br.com.servicetrack.domain.shared.enums

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class IndicativoSimNaoTest {

    @Test
    fun `deve retornar S quando codigo for 1`() {
        val resultado = IndicativoSimNao.fromCodigo(1)
        assertEquals(IndicativoSimNao.S, resultado)
    }

    @Test
    fun `deve retornar N quando codigo for 0`() {
        val resultado = IndicativoSimNao.fromCodigo(0)
        assertEquals(IndicativoSimNao.N, resultado)
    }

    @Test
    fun `deve lançar IllegalArgumentException quando codigo for inválido`() {
        val ex = assertThrows<IllegalArgumentException> {
            IndicativoSimNao.fromCodigo(99)
        }
        assertEquals("Código inválido para IndicativoSimNao: 99", ex.message)
    }

    @Test
    fun `S deve ter codigo 1 e descricao Sim`() {
        assertEquals(1, IndicativoSimNao.S.codigo)
        assertEquals("Sim", IndicativoSimNao.S.descricao)
    }

    @Test
    fun `N deve ter codigo 0 e descricao Não`() {
        assertEquals(0, IndicativoSimNao.N.codigo)
        assertEquals("Não", IndicativoSimNao.N.descricao)
    }
}
