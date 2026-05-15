package br.com.servicetrack.application.dashboard.dto.query

import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrdemServicoDashboardQueryDTO(
    val id: String,
    val motivo: String,
    val status: StatusOrdemServicoEnum,
    val veiculoId: String,
    val veiculoPlaca: String,
    val veiculoModelo: String,
    val mecanicoId: String,
    val mecanicoNome: String,
    val dataCriacao: LocalDateTime,
    val dataAtualizacao: LocalDateTime,
    val prazoConclusao: LocalDateTime?,
    val valorOrcado: BigDecimal?,
)
