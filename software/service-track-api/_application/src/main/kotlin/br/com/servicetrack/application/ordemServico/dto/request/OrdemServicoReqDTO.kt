package br.com.servicetrack.application.ordemServico.dto.request

import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

data class OrdemServicoReqDTO (
    val motivo: String,
    @field:Rastreavel
    val clienteId: UsuarioId,
    @field:Rastreavel
    val mecanicoId: UsuarioId,
    @field:Rastreavel
    val veiculoId: VeiculoId,
    val observaco: String?
)
