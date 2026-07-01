package br.com.servicetrack.application.veiculo.dto.fipe

data class DetalheVeiculoFipeDTO(
    val codigoFipe: String,
    val marca: String,
    val modelo: String,
    val anoModelo: Int,
    val combustivel: String,
    val valor: String,
    val mesReferencia: String
)
