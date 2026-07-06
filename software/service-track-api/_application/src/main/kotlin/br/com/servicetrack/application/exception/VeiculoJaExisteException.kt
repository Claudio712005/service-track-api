package br.com.servicetrack.application.exception

class VeiculoJaExisteException(placa: String): RuntimeException(
    "Veículo com a placa $placa já cadastrado."
)
