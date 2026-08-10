package br.com.servicetrack.application.usuario.dto.request

import br.com.servicetrack.application.observabilidade.annotation.Mascarado
import java.time.LocalDate

data class CadastrarClienteReqDTO(
    val nome: String,
    val email: String,
    val senha: String,
    val telefone: String,
    @field:Mascarado(visiveis = 2)
    val cpf: String,
    val dataNascimento: LocalDate
)
