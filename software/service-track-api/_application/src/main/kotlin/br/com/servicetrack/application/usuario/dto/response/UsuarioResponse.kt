package br.com.servicetrack.application.usuario.dto.response

import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario

data class UsuarioResponse(
    val id: String,
    val nome: String,
    val email: String,
    val cpf: String,
    val telefone: String,
    val roles: Set<Role>,
    val ativo: Boolean
) {
    companion object {
        fun de(usuario: Usuario): UsuarioResponse {
            val dados = usuario.obterDados()
            return UsuarioResponse(
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
