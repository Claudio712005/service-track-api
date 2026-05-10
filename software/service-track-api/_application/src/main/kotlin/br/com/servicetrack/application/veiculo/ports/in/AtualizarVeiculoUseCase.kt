package br.com.servicetrack.application.veiculo.ports.`in`

import br.com.servicetrack.application.veiculo.dto.request.AtualizarVeiculoReqDTO
import br.com.servicetrack.application.veiculo.dto.response.DadosveiculoResDTO
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

interface AtualizarVeiculoUseCase {
    fun atualizarVeiculo(id: VeiculoId, req: AtualizarVeiculoReqDTO): DadosveiculoResDTO
}
