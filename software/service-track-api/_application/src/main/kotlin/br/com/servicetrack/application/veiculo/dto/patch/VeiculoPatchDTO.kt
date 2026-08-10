package br.com.servicetrack.application.veiculo.dto.patch

import br.com.servicetrack.application.observabilidade.annotation.Mascarado
import br.com.servicetrack.domain.shared.enums.IndicativoSimNao
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.Placa

data class VeiculoPatchDTO(
    @field:Mascarado(visiveis = 3)
    val placa: Placa?,
    val proprietarioId: UsuarioId?,
    val ativo: IndicativoSimNao?
)
