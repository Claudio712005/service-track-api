package br.com.servicetrack.application.veiculo.ports.`in`

import br.com.servicetrack.application.veiculo.dto.request.CadastrarVeiculoReqDTO
import br.com.servicetrack.application.veiculo.dto.response.DadosveiculoResDTO

interface CadastrarVeiculoUseCase {

    fun cadastrarVeiculo(veiculo: CadastrarVeiculoReqDTO): DadosveiculoResDTO
}
