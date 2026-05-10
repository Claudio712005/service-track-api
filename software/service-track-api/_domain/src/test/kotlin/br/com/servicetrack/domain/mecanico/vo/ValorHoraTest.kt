package br.com.servicetrack.domain.mecanico.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class ValorHoraTest {

    @Test
    fun `deve criar ValorHora quando valor válido`(){
        val valorHora = ValorHora(valor = BigDecimal(10))

        assertEquals(BigDecimal.valueOf(10), valorHora.valor)
    }

    @Test
    fun `deve lançar exceção quando valor for zero ou negativo`(){
        try {
            ValorHora(valor = BigDecimal.ZERO)
        } catch (e: Exception) {
            assert(e is DomainException)
            assertEquals("Valor da hora deve ser maior que zero", e.message)
        }

        try {
            ValorHora(valor = BigDecimal(-5))
        } catch (e: Exception) {
            assert(e is DomainException)
            assertEquals("Valor da hora deve ser maior que zero", e.message)
        }
    }
}