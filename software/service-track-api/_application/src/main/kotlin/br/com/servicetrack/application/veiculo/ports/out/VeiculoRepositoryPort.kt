package br.com.servicetrack.application.veiculo.ports.out

import br.com.servicetrack.application.veiculo.dto.patch.VeiculoPatchDTO
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

interface VeiculoRepositoryPort {

    fun salvar(veiculo: Veiculo)
    fun existeVeiculoPorPlaca(placa: String): Boolean
    fun buscarPorId(id: VeiculoId): Veiculo?
    fun desativar(id: VeiculoId)
    fun atualizar(veiculo: Veiculo)
    fun listarPorProprietario(proprietarioId: UsuarioId): List<Veiculo>
    fun listarTodos(): List<Veiculo>
}