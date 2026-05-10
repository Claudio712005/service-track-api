package br.com.servicetrack.application.servico.dto

import br.com.servicetrack.domain.servico.UnidadeTempoEnum

data class TempoMedioResDTO(
    val servicoId: String,
    val tempoMedio: Double,
    val unidade: UnidadeTempoEnum,
    val totalAmostras: Int,
)
