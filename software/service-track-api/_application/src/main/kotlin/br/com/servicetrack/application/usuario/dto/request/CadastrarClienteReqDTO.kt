package br.com.servicetrack.application.usuario.dto.request

import java.time.LocalDate

data class CadastrarClienteReqDTO(
    val nome: String,
    val email: String,
    val senha: String,
    val telefone: String,
    val cpf: String,
    val dataNascimento: LocalDate
)
