package br.com.servicetrack.application.orcamento.dto.res

import br.com.servicetrack.domain.orcamento.vo.OrcamentoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import java.time.LocalDateTime

data class OrcamentoResDTO(
    val id: OrcamentoId,
    val dataCriacao: LocalDateTime,
    val custoMaoDeObra: ValorMonetario,
    val custoInsumo: ValorMonetario,
)