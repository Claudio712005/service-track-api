package br.com.servicetrack.application.ordemServico.ports.out

import br.com.servicetrack.application.dashboard.dto.query.OrdemServicoDashboardQueryDTO
import br.com.servicetrack.application.ordemServico.dto.request.FiltroOrdemServicoDTO
import br.com.servicetrack.application.shared.dto.PageResDTO
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import java.math.BigDecimal

interface OrdemServicoRepositoryPort {

    fun salvar(ordemServico: OrdemServico): OrdemServico
    fun atualizar(ordemServico: OrdemServico): OrdemServico
    fun buscarPorId(id: OrdemServicoId): OrdemServico?
    fun listar(filtro: FiltroOrdemServicoDTO): PageResDTO<OrdemServico>
    fun contarOsAbertaPorIdVeiculoEIdCliente(clienteId: UsuarioId, veiculoId: VeiculoId): Long
    fun listarAtivasDashboardPorCliente(clienteId: UsuarioId, limit: Int): List<OrdemServicoDashboardQueryDTO>
    fun listarRecentesDashboardPorCliente(clienteId: UsuarioId, limit: Int): List<OrdemServicoDashboardQueryDTO>
    fun contarPorClienteEStatus(clienteId: UsuarioId, statuses: List<StatusOrdemServicoEnum>): Long
    fun contarTotalPorVeiculo(veiculoId: VeiculoId, clienteId: UsuarioId): Long
    fun somarGastoPorVeiculo(veiculoId: VeiculoId, clienteId: UsuarioId): BigDecimal
}