package br.com.servicetrack.application.ordemServico.ports.`in`

import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO

interface GerarOrcamentoUseCase {
    fun gerarOrcamento(ordemServicoId: String): OrdemServicoResDTO
}
