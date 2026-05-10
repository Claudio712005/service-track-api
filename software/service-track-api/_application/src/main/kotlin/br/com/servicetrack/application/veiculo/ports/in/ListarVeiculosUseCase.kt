package br.com.servicetrack.application.veiculo.ports.`in`

import br.com.servicetrack.application.veiculo.dto.response.DadosveiculoResDTO

interface ListarVeiculosUseCase {
    fun listarVeiculos(): List<DadosveiculoResDTO>
}
