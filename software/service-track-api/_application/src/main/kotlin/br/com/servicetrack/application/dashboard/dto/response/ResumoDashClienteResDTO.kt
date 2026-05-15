package br.com.servicetrack.application.dashboard.dto.response

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
    val id: String,
    val motivo: String,
    val status: String,
    val veiculoId: String,
    val veiculoPlaca: String,
    val veiculoModelo: String,
    val mecanicoId: String,
    val mecanicoNome: String,
    val dataCriacao: LocalDateTime,
    val dataAtualizacao: LocalDateTime,
    val diasEmAndamento: Int,
    val valorOrcado: BigDecimal?,
    val prazoConclusao: LocalDateTime?,
)

data class OrdensRecentesResDTO(
    val id: String,
    val motivo: String,
    val status: StatusOrdemServicoEnum,
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
    val id: String,
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
    val usuarioId: String,
    val usuarioNome: String,
    val resumo: ResumoResDTO,
    val ordensAtivas: List<OrdensAtivasResDTO>,
    val ordensRecentes: List<OrdensRecentesResDTO>,
    val veiculos: List<VeiculoDashResDTO>,
    val dataAtualizacao: LocalDateTime,
)
