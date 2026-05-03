package br.com.servicetrack.application.servico.ports.`in`

import br.com.servicetrack.application.servico.dto.TempoMedioResDTO
import br.com.servicetrack.domain.servico.UnidadeTempoEnum

interface BuscarTempoMedioConclusaoUseCase {
    fun buscarTempoMedio(servicoId: String, unidade: UnidadeTempoEnum): TempoMedioResDTO
}
