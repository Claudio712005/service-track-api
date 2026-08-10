package br.com.servicetrack.application.dashboard.dto.query

import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrdemServicoDashboardQueryDTO(
    @field:Rastreavel
    val id: String,
    val motivo: String,
    @field:Rastreavel
    val status: StatusOrdemServicoEnum,
    @field:Rastreavel
    val veiculoId: String,
    val veiculoPlaca: String,
    val veiculoModelo: String,
    @field:Rastreavel
    val mecanicoId: String,
    val mecanicoNome: String,
    val dataCriacao: LocalDateTime,
    val dataAtualizacao: LocalDateTime,
    val prazoConclusao: LocalDateTime?,
    val valorOrcado: BigDecimal?,
)
