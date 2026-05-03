package br.com.servicetrack.domain.shared.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals

class ValorMonetarioTest {

    @Test
    fun `deve criar valor monetário válido`() {
        val valor = ValorMonetario(BigDecimal("100.00"))
        assertEquals(BigDecimal("100.00"), valor.valor)
    }

    @Test
    fun `deve criar valor monetário zero`() {
        val valor = ValorMonetario.zero()
        assertEquals(BigDecimal.ZERO, valor.valor)
    }

    @Test
    fun `deve criar valor monetário positivo`() {
        val valor = ValorMonetario(BigDecimal("0.01"))
        assertEquals(BigDecimal("0.01"), valor.valor)
    }

    @Test
    fun `deve lançar exceção para valor negativo`() {
        val exception = assertThrows<DomainException> {
            ValorMonetario(BigDecimal("-1.00"))
        }
        assertEquals("Valor monetário não pode ser negativo", exception.message)
    }

    @Test
    fun `deve somar dois valores monetários`() {
        val v1 = ValorMonetario(BigDecimal("100.00"))
        val v2 = ValorMonetario(BigDecimal("50.00"))
        assertEquals(BigDecimal("150.00"), v1.somar(v2).valor)
    }

    @Test
    fun `deve somar com zero sem alterar valor`() {
        val v1 = ValorMonetario(BigDecimal("100.00"))
        assertEquals(BigDecimal("100.00"), v1.somar(ValorMonetario.zero()).valor)
    }

    @Test
    fun `deve multiplicar valor por fator inteiro`() {
        val valor = ValorMonetario(BigDecimal("25.00"))
        assertEquals(BigDecimal("75.00"), valor.multiplicar(BigDecimal("3")).valor)
    }

    @Test
    fun `deve multiplicar valor por fator fracionário`() {
        val valor = ValorMonetario(BigDecimal("100.00"))
        assertEquals(BigDecimal("33.33"), valor.multiplicar(BigDecimal("0.3333")).valor)
    }

    @Test
    fun `deve arredondar para cima com HALF_UP`() {
        val valor = ValorMonetario(BigDecimal("10.00"))
        assertEquals(BigDecimal("33.34"), valor.multiplicar(BigDecimal("3.334")).valor)
    }

    @Test
    fun `deve multiplicar por um sem alterar valor`() {
        val valor = ValorMonetario(BigDecimal("99.99"))
        assertEquals(BigDecimal("99.99"), valor.multiplicar(BigDecimal("1")).valor)
    }
}
