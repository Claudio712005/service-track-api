package br.com.servicetrack.infrastructure.usuario.persistence

import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "usuarios")
class UsuarioEntity : PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    lateinit var id: UUID

    @Column(name = "nome", nullable = false)
    lateinit var nome: String

    @Column(name = "email", nullable = false, unique = true)
    lateinit var email: String

    @Column(name = "senha_hash", nullable = false)
    lateinit var senhaHash: String

    @Column(name = "data_criacao", nullable = false, updatable = false)
    lateinit var dataCriacao: LocalDateTime

    @Column(name = "data_atualizacao", nullable = false)
    lateinit var dataAtualizacao: LocalDateTime

    @Column(name = "data_nascimento", nullable = false)
    lateinit var dataNascimento: LocalDate

    @Column(name = "telefone", nullable = false)
    lateinit var telefone: String

    @Column(name = "cpf", nullable = false, unique = true)
    lateinit var cpf: String

    @Column(name = "ativo", nullable = false)
    var ativo: Boolean = true

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = [JoinColumn(name = "usuario_id")])
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    var roles: MutableSet<Role> = mutableSetOf()

    companion object : PanacheCompanion<UsuarioEntity> {
        fun de(usuario: Usuario): UsuarioEntity {
            val dados = usuario.obterDados()
            return UsuarioEntity().apply {
                id = UUID.fromString(dados.id.valor)
                nome = dados.nome
                email = dados.email.valor
                senhaHash = usuario.obterSenhaHash().valor
                dataCriacao = LocalDateTime.now()
                dataAtualizacao = LocalDateTime.now()
                dataNascimento = dados.dataNascimento
                telefone = dados.telefone.valor
                cpf = dados.cpf.valor
                ativo = dados.ativo
                roles = dados.roles.toMutableSet()
            }
        }

    }
}
