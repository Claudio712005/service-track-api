package br.com.servicetrack.application.servico.dto

import br.com.servicetrack.domain.servico.Servico
import java.math.BigDecimal

data class ServicoResDTO(
    val id: String,
    val nomeServico: String,
    val descricaoServico: String,
    val valorReferencia: BigDecimal?
) {
    companion object {
        fun de(servico: Servico) = ServicoResDTO(
            id = servico.id.valor,
            nomeServico = servico.nomeServico,
            descricaoServico = servico.descricaoServico,
            valorReferencia = servico.valorReferencia?.valor
        )
    }
}
