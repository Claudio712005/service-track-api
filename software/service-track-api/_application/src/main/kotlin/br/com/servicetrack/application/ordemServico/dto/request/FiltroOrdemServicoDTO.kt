package br.com.servicetrack.application.ordemServico.dto.request

import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum

data class FiltroOrdemServicoDTO(
    val status: StatusOrdemServicoEnum? = null,
    val clienteId: String? = null,
    val mecanicoId: String? = null,
    val page: Int = 0,
    val size: Int = 20,
)
