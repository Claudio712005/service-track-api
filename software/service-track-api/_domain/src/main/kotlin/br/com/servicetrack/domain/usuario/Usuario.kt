package br.com.servicetrack.domain.usuario

import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import java.time.LocalDate
import java.time.LocalDateTime

class Usuario private constructor(
    val id: UsuarioId,
    private var nome: String,
    private var email: Email,
    private var senha: Senha,
    private val dataCriacao: LocalDateTime,
    private var dataAtualizacao: LocalDateTime,
    private var dataNascimento: LocalDate,
    private var telefone: Telefone,
    private var cpf: Cpf,
    private var ativo: Boolean,
    private val roles: MutableSet<Role>
) {

    companion object {

        fun criar(
            nome: String,
            email: Email,
            senha: Senha,
            dataNascimento: LocalDate,
            telefone: Telefone,
            cpf: Cpf,
            roles: Set<Role>
        ): Usuario {
            require(nome.isNotBlank()) { "Nome não pode ser vazio" }
            require(roles.isNotEmpty()) { "Usuário deve possuir pelo menos um perfil" }

            val agora = LocalDateTime.now()

            return Usuario(
                id = UsuarioId.gerar(),
                nome = nome,
                email = email,
                senha = senha,
                dataCriacao = agora,
                dataAtualizacao = agora,
                dataNascimento = dataNascimento,
                telefone = telefone,
                cpf = cpf,
                ativo = true,
                roles = roles.toMutableSet()
            )
        }

        fun criarCliente(
            nome: String,
            email: Email,
            senha: Senha,
            dataNascimento: LocalDate,
            telefone: Telefone,
            cpf: Cpf,
        ): Usuario = criar(
            nome = nome,
            email = email,
            senha = senha,
            dataNascimento = dataNascimento,
            telefone = telefone,
            cpf = cpf,
            roles = setOf(Role.CLIENTE)
        )
    }

    fun desativar() {
        check(ativo) { "Usuário já está desativado" }
        ativo = false
        atualizarData()
    }

    fun ativar() {
        check(!ativo) { "Usuário já está ativo" }
        ativo = true
        atualizarData()
    }

    fun alterarSenha(novaSenha: Senha) {
        senha = novaSenha
        atualizarData()
    }

    fun adicionarRole(role: Role) {
        roles.add(role)
    }

    fun ehCliente() = roles.contains(Role.CLIENTE)

    fun ehMecanico() = roles.contains(Role.MECANICO)

    private fun atualizarData() {
        dataAtualizacao = LocalDateTime.now()
    }
}
