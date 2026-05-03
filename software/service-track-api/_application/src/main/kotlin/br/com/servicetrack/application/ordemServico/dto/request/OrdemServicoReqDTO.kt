package br.com.servicetrack.application.ordemServico.dto.request

import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

data class OrdemServicoReqDTO (
    val motivo: String,
    val clienteId: UsuarioId,
    val mecanicoId: UsuarioId,
    val veiculoId: VeiculoId,
    val observaco: String?
)