package br.com.servicetrack.infrastructure.veiculo

import br.com.servicetrack.application.veiculo.dto.request.CadastrarVeiculoReqDTO
import br.com.servicetrack.infrastructure.api.dto.CadastrarVeiculoRequest

internal fun CadastrarVeiculoRequest.toApplicationDTO() = CadastrarVeiculoReqDTO(
    placa = placa,
    modelo = modelo,
    marca = marca,
    ano = ano,
    proprietarioId = proprietarioId,
)