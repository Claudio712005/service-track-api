package br.com.servicetrack.application.veiculo.dto.request

data class CadastrarVeiculoReqDTO(
   val placa: String,
    val marca: String,
    val modelo: String,
    val ano: Int,
    val proprietarioId: String,
)
