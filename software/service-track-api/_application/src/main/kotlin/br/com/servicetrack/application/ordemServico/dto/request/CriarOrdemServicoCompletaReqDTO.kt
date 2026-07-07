package br.com.servicetrack.application.ordemServico.dto.request

import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

data class CriarOrdemServicoCompletaReqDTO(
    val motivo: String,
    val clienteId: UsuarioId,
    val veiculoId: VeiculoId,
    val observacao: String?,
    val servicos: List<ItemServicoReqDTO>,
    val insumos: List<ItemInsumoReqDTO>,
)
