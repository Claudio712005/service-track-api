package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.veiculo.dto.response.DadosveiculoResDTO
import br.com.servicetrack.application.veiculo.ports.`in`.BuscarVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.`out`.VeiculoRepositoryPort
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

class BuscarVeiculoService(
    private val repository: VeiculoRepositoryPort
) : BuscarVeiculoUseCase {

    override fun buscarVeiculo(id: VeiculoId): DadosveiculoResDTO {
        val veiculo = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Veiculo::class.java.name, arrayOf(id.valor))
        return DadosveiculoResDTO.de(veiculo)
    }
}
