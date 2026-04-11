package br.com.servicetrack.application.veiculo.ports.`in`

import br.com.servicetrack.domain.veiculo.vo.VeiculoId

interface RemoverVeiculoUseCase {

    fun removerVeiculo(veiculo: VeiculoId)
}