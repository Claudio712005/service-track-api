package br.com.servicetrack.application.servico.dto

import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import br.com.servicetrack.domain.servico.UnidadeTempoEnum

data class TempoMedioResDTO(
    @field:Rastreavel
    val servicoId: String,
    val tempoMedio: Double,
    val unidade: UnidadeTempoEnum,
    val totalAmostras: Int,
)
