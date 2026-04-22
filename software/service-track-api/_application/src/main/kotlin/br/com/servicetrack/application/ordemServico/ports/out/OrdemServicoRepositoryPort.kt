package br.com.servicetrack.application.ordemServico.ports.out

import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

interface OrdemServicoRepositoryPort {

    fun salvar(ordemServico: OrdemServico): OrdemServico
    fun contarOsAbertaPorIdVeiculoEIdCliente(clienteId: UsuarioId, veiculoId: VeiculoId): Long
}