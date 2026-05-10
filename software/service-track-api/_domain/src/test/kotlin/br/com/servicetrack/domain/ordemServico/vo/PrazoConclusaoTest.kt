package br.com.servicetrack.domain.ordemServico.vo

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrazoConclusaoTest {

    @Test
    fun `deve calcular horas restantes corretamente`() {
        val agora = LocalDateTime.now()
        val prazo = PrazoConclusao(agora.plusHours(10))

        val horasRestantes = prazo.horasRestantes(agora)

        assertEquals(10L, horasRestantes)
    }

    @Test
    fun `deve retornar horas negativas quando prazo ja passou`() {
        val agora = LocalDateTime.now()
        val prazo = PrazoConclusao(agora.minusHours(5))

        val horasRestantes = prazo.horasRestantes(agora)

        assertTrue(horasRestantes < 0)
    }
}
