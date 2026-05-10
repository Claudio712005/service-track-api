package br.com.servicetrack.application.servico.dto

import br.com.servicetrack.domain.servico.Servico

data class ServicoResumoResDTO(
    val id: String,
    val nomeServico: String,
    val descricaoServico: String
) {
    companion object {
        fun de(servico: Servico) = ServicoResumoResDTO(
            id = servico.id.valor,
            nomeServico = servico.nomeServico,
            descricaoServico = servico.descricaoServico
        )
    }
}
