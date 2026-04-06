package br.com.servicetrack.application.mecanico.dto.request

import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import java.math.BigDecimal
import java.time.LocalDate

data class CadastrarMecanicoReqDTO(
    val nome: String,
    val email: String,
    val senha: String,
    val telefone: String,
    val cpf: String,
    val dataNascimento: LocalDate,
    val nivel: NivelMecanicoEnum,
    val valorHora: BigDecimal
)
