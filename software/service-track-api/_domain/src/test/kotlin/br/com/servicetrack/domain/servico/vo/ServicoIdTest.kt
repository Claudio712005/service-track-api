package br.com.servicetrack.domain.servico.vo

import br.com.servicetrack.domain.orcamento.vo.OrcamentoId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

class ServicoIdTest {

    @Test
    fun `deve gerar ServicoId com UUID valido`(){
        val id = ServicoId.gerar()

        assertDoesNotThrow {
            UUID.fromString(id.valor)
        }
    }
}