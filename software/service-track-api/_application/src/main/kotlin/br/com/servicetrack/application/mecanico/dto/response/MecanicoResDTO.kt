package br.com.servicetrack.application.mecanico.dto.response

import br.com.servicetrack.domain.mecanico.Mecanico
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import java.math.BigDecimal

data class MecanicoResDTO(
    val usuarioId: String,
    val nome: String,
    val email: String,
    val cpf: String,
    val telefone: String,
    val nivel: String,
    val valorHora: BigDecimal,
    val roles: Set<Role>,
    val ativo: Boolean
) {
    companion object {
        fun de(usuario: Usuario, mecanico: Mecanico): MecanicoResDTO {
            val dados = usuario.obterDados()
            return MecanicoResDTO(
                usuarioId = dados.id.valor,
                nome = dados.nome,
                email = dados.email.valor,
                cpf = dados.cpf.valor,
                telefone = dados.telefone.valor,
                nivel = mecanico.obterNivel().valor.name,
                valorHora = mecanico.obterValorHora().valor,
                roles = dados.roles,
                ativo = dados.ativo
            )
        }
    }
}
