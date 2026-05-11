package br.com.servicetrack.infrastructure.veiculo

import br.com.servicetrack.application.veiculo.dto.request.AtualizarVeiculoReqDTO
import br.com.servicetrack.application.veiculo.dto.request.CadastrarVeiculoReqDTO
import br.com.servicetrack.application.veiculo.dto.response.DadosveiculoResDTO
import br.com.servicetrack.infrastructure.api.dto.AtualizarVeiculoRequest
import br.com.servicetrack.infrastructure.api.dto.CadastrarVeiculoRequest
import br.com.servicetrack.infrastructure.api.dto.DadosVeiculoResponse
import java.util.UUID

internal fun CadastrarVeiculoRequest.toApplicationDTO() = CadastrarVeiculoReqDTO(
    placa = placa,
    modelo = modelo,
    marca = marca,
    ano = ano,
    proprietarioId = proprietarioId.toString(),
    urlImagem = urlImagem
)

internal fun AtualizarVeiculoRequest.toApplicationDTO() = AtualizarVeiculoReqDTO(
    placa = placa,
    modelo = modelo,
    marca = marca,
    ano = ano,
    urlImagem = urlImagem
)

internal fun DadosveiculoResDTO.toDadosVeiculoResponse(): DadosVeiculoResponse = DadosVeiculoResponse()
    .id(UUID.fromString(id))
    .placa(placa)
    .modelo(modelo)
    .marca(marca)
    .ano(ano)
    .proprietarioId(UUID.fromString(proprietarioId))
    .urlImagem(urlImagem)
    .codigoFipe(codigoFipe)
