package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO

interface EnviarParaDiagnosticoUseCase {
    fun enviarParaDiagnostico(ordemServicoId: String): ResumoOrdemServicoResDTO
}
