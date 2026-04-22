package br.com.servicetrack.infrastructure.ordemServico

import br.com.servicetrack.application.ordemServico.dto.request.OrdemServicoReqDTO
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import br.com.servicetrack.infrastructure.api.dto.OrdemServicoRequest

internal fun OrdemServicoRequest.toApplicationDTO() = OrdemServicoReqDTO(
    motivo,
    UsuarioId(clienteId),
    UsuarioId(mecanicoId),
    VeiculoId(veiculoId),
    observacao
)