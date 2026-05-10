package br.com.servicetrack.domain.veiculo.vo

import br.com.servicetrack.domain.servico.vo.ServicoId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

class VeiculoIdTest {

    @Test
    fun `deve gerar VeiculoId com UUID valido`() {
        val id = ServicoId.gerar()

        assertDoesNotThrow {
            UUID.fromString(id.valor)
        }
    }
}