package br.com.servicetrack.application.ordemServico.ports.out

import br.com.servicetrack.application.ordemServico.dto.request.FiltroOrdemServicoDTO
import br.com.servicetrack.application.shared.dto.PageResDTO
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

interface OrdemServicoRepositoryPort {

    fun salvar(ordemServico: OrdemServico): OrdemServico
    fun atualizar(ordemServico: OrdemServico): OrdemServico
    fun buscarPorId(id: OrdemServicoId): OrdemServico?
    fun listar(filtro: FiltroOrdemServicoDTO): PageResDTO<OrdemServico>
    fun contarOsAbertaPorIdVeiculoEIdCliente(clienteId: UsuarioId, veiculoId: VeiculoId): Long
}