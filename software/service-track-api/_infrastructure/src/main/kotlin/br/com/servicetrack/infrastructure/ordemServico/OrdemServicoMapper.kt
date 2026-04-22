package br.com.servicetrack.infrastructure.ordemServico

import br.com.servicetrack.application.ordemServico.dto.request.AssociarItensReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.CancelarOsReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.ConcluirItemServicoReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.FiltroOrdemServicoDTO
import br.com.servicetrack.application.ordemServico.dto.request.ItemInsumoReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.ItemServicoReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.OrdemServicoReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.ReprovarOrcamentoReqDTO
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import br.com.servicetrack.infrastructure.api.dto.AssociarItensRequest
import br.com.servicetrack.infrastructure.api.dto.CancelarOsRequest
import br.com.servicetrack.infrastructure.api.dto.ConcluirItemServicoRequest
import java.math.BigDecimal
import br.com.servicetrack.infrastructure.api.dto.OrdemServicoRequest
import br.com.servicetrack.infrastructure.api.dto.ReprovarOrcamentoRequest

internal fun OrdemServicoRequest.toApplicationDTO() = OrdemServicoReqDTO(
    motivo,
    UsuarioId(clienteId),
    UsuarioId(mecanicoId),
    VeiculoId(veiculoId),
    observacao,
)

internal fun AssociarItensRequest.toApplicationDTO() = AssociarItensReqDTO(
    servicos = servicos.map { ItemServicoReqDTO(it.servicoId.toString(), it.valorCobrado?.let { v -> BigDecimal.valueOf(v) }) },
    insumos = insumos.map { ItemInsumoReqDTO(it.insumoId.toString(), it.quantidade ?: 1) },
)

internal fun ConcluirItemServicoRequest.toApplicationDTO() = ConcluirItemServicoReqDTO(observacao)

internal fun ReprovarOrcamentoRequest.toApplicationDTO() = ReprovarOrcamentoReqDTO(motivo)

internal fun CancelarOsRequest?.toApplicationDTO() = CancelarOsReqDTO(this?.motivo)

internal fun toFiltroDTO(
    status: String?,
    clienteId: String?,
    mecanicoId: String?,
    page: Int,
    size: Int,
) = FiltroOrdemServicoDTO(
    status = status?.let { StatusOrdemServicoEnum.valueOf(it) },
    clienteId = clienteId,
    mecanicoId = mecanicoId,
    page = page,
    size = size,
)
