package br.com.servicetrack.application.veiculo.dto.request

import br.com.servicetrack.application.observabilidade.annotation.Mascarado

data class CadastrarVeiculoReqDTO(
    @field:Mascarado(visiveis = 3)
    val placa: String,
    val marca: String,
    val modelo: String,
    val ano: Int,
    val proprietarioId: String,
    val urlImagem: String? = null
)
