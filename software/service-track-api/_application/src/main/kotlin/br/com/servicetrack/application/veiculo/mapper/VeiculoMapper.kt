package br.com.servicetrack.application.veiculo.mapper

import br.com.servicetrack.application.veiculo.dto.request.CadastrarVeiculoReqDTO
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.Placa

internal fun CadastrarVeiculoReqDTO.toDomain() = Veiculo.criar(
    proprietarioId = UsuarioId(proprietarioId),
    placa = Placa(placa),
    modelo = modelo,
    marca = marca,
    ano = ano
)