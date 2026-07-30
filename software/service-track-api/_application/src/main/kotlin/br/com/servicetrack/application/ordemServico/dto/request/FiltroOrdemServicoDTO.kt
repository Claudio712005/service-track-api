package br.com.servicetrack.application.ordemServico.dto.request

import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum

data class FiltroOrdemServicoDTO(
    @field:Rastreavel
    val status: StatusOrdemServicoEnum? = null,
    @field:Rastreavel
    val clienteId: String? = null,
    @field:Rastreavel
    val mecanicoId: String? = null,
    val page: Int = 0,
    val size: Int = 20,
)
