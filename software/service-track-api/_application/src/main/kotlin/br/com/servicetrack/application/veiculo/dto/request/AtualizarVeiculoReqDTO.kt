package br.com.servicetrack.application.veiculo.dto.request

import br.com.servicetrack.application.observabilidade.annotation.Mascarado

data class AtualizarVeiculoReqDTO(
    @field:Mascarado(visiveis = 3)
    val placa: String? = null,
    val modelo: String? = null,
    val marca: String? = null,
    val ano: Int? = null,
    val urlImagem: String? = null
)
