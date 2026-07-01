package br.com.servicetrack.application.dashboard.dto.query

import java.time.LocalDateTime

data class VeiculoDashboardQueryDTO(
    val id: String,
    val placa: String,
    val marca: String,
    val modelo: String,
    val ano: Int,
    val imagemUrl: String?,
    val codigoFipe: String?,
    val ativo: Boolean,
    val dataCriacao: LocalDateTime,
)
