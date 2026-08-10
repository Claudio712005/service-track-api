package br.com.servicetrack.application.usuario.dto.response

import br.com.servicetrack.application.observabilidade.annotation.Mascarado
import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario

data class ClienteResDTO(
    @field:Rastreavel
    val id: String,
    val nome: String,
    val email: String,
    @field:Mascarado(visiveis = 2)
    val cpf: String,
    val telefone: String,
    val roles: Set<Role>,
    val ativo: Boolean
) {
    companion object {
        fun de(usuario: Usuario): ClienteResDTO {
            val dados = usuario.obterDados()
            return ClienteResDTO(
                id = dados.id.valor,
                nome = dados.nome,
                email = dados.email.valor,
                cpf = dados.cpf.valor,
                telefone = dados.telefone.valor,
                roles = dados.roles,
                ativo = dados.ativo
            )
        }
    }
}
