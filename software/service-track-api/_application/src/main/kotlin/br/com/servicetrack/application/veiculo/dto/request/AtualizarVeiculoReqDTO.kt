package br.com.servicetrack.application.veiculo.dto.request

data class AtualizarVeiculoReqDTO(
    val placa: String? = null,
    val modelo: String? = null,
    val marca: String? = null,
    val ano: Int? = null
)
