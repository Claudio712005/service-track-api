package br.com.servicetrack.domain.veiculo.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

class PlacaTest {

    @Test
    fun `deve criar uma placa no formato Mercosul`() {
        val placa = Placa("ABC1D23")
        assertNotNull(placa)
    }

    @Test
    fun `deve criar uma placa no formato antigo`() {
        val placa = Placa("ABC1234")
        assertNotNull(placa)
    }

    @Test
    fun `deve lançar exceção quando a placa for inválida`() {
        assertThrows<DomainException> {
            Placa("1234ABC")
        }
    }

    @Test
    fun `deve lançar exceção quando a placa estiver vazia`() {
        assertThrows<DomainException> {
            Placa("")
        }
    }
}
