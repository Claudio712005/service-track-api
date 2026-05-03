package br.com.servicetrack.domain.mecanico.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


class HorasTrabalhoTest {

    @Test
    fun `deve lançar exceção quando valor é menor que zero`(){
        val exception = assertThrows<DomainException> {
            HorasTrabalho(valor = -1)
        }

        assertEquals("Horas de trabalho devem ser maior que zero", exception.message)
    }

    @Test
    fun `deve criar HorasTrabalho válido`(){
        val horas = HorasTrabalho(valor = 8)
        assertEquals(8, horas.valor)
    }
}