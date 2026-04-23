package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO
import java.time.LocalDate

interface GerarOrcamentoUseCase {
    fun gerarOrcamento(ordemServicoId: String, prazoConclusao: LocalDate): OrdemServicoResDTO
}
