package br.com.servicetrack.application.veiculo.ports.`in`

import br.com.servicetrack.application.veiculo.dto.response.DadosveiculoResDTO
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

interface BuscarVeiculoUseCase {
    fun buscarVeiculo(id: VeiculoId): DadosveiculoResDTO
}
