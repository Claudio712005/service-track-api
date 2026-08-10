package br.com.servicetrack.application.dashboard.dto.response

import br.com.servicetrack.application.observabilidade.annotation.Mascarado
import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import java.math.BigDecimal
import java.time.LocalDateTime

data class ResumoResDTO(
    val ordensAtivas: Int,
    val ordensConcluidas: Int,
    val ordensCanceladas: Int,
    val totalOrdens: Int,
    val veiculosCadastrados: Int,
)

data class OrdensAtivasResDTO(
    @field:Rastreavel
    val id: String,
    val motivo: String,
    @field:Rastreavel
    val status: String,
    @field:Rastreavel
    val veiculoId: String,
    val veiculoPlaca: String,
    val veiculoModelo: String,
    @field:Rastreavel
    val mecanicoId: String,
    val mecanicoNome: String,
    val dataCriacao: LocalDateTime,
    val dataAtualizacao: LocalDateTime,
    val diasEmAndamento: Int,
    val valorOrcado: BigDecimal?,
    val prazoConclusao: LocalDateTime?,
)

data class OrdensRecentesResDTO(
    @field:Rastreavel
    val id: String,
    val motivo: String,
    @field:Rastreavel
    val status: StatusOrdemServicoEnum,
    @field:Rastreavel
    val veiculoId: String,
    val veiculoPlaca: String,
    val veiculoModelo: String,
    val dataCriacao: LocalDateTime,
    val dataConclusao: LocalDateTime?,
    val diasParaConclusao: Int?,
    val valorTotal: BigDecimal?,
    val mecanicoNome: String?,
)

data class VeiculoDashResDTO(
    @field:Rastreavel
    val id: String,
    @field:Mascarado(visiveis = 3)
    val placa: String,
    val marca: String,
    val modelo: String,
    val ano: Int,
    val imagemUrl: String?,
    val codigoFipe: String?,
    val ativo: Boolean,
    val totalOrdens: Int,
    val totalGasto: BigDecimal,
    val dataCriacao: LocalDateTime,
)

data class ResumoDashClienteResDTO(
    @field:Rastreavel
    val usuarioId: String,
    val usuarioNome: String,
    val resumo: ResumoResDTO,
    val ordensAtivas: List<OrdensAtivasResDTO>,
    val ordensRecentes: List<OrdensRecentesResDTO>,
    val veiculos: List<VeiculoDashResDTO>,
    val dataAtualizacao: LocalDateTime,
)
