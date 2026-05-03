package br.com.servicetrack.domain.ordemServico.vo

import br.com.servicetrack.domain.orcamento.vo.OrcamentoId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

class OrdemServicoIdTest {

    @Test
    fun `deve gerar OrcamentoId com UUID valido`(){
        val id = OrcamentoId.gerar()

        assertDoesNotThrow {
            UUID.fromString(id.valor)
        }
    }
}