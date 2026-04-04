package br.com.servicetrack.domain.veiculo.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.Test

class PlacaTest {

    @Test
    fun `deve criar uma placa quando a placa for válida`() {
        Placa("ABC1D23")
    }

    @Test(expected = DomainException::class)
    fun `deve lançar exceção quando a placa for inválida`() {
        Placa("1234ABC")
    }
}