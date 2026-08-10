package br.com.servicetrack.application.dashboard.dto.query

import br.com.servicetrack.application.observabilidade.annotation.Mascarado
import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import java.time.LocalDateTime

data class VeiculoDashboardQueryDTO(
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
    val dataCriacao: LocalDateTime,
)
