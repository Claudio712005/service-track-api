package br.com.servicetrack.infrastructure.usuario.persistence

import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class UsuarioRepositoryAdapter : UsuarioRepositoryPort {

    override fun salvar(usuario: Usuario) {
        UsuarioEntity.de(usuario).persist()
    }

    override fun existePorEmailOuCpf(email: String, cpf: String): Boolean =
        UsuarioEntity.count("(email = ?1 or cpf = ?2) and ativo = ?3", email, cpf, true) > 0

    override fun buscarPorEmail(email: String): Usuario? =
        UsuarioEntity.find("email", email).firstResult()?.toDomain()

    override fun buscarPorId(id: UsuarioId): Usuario? =
        UsuarioEntity.find("id", UUID.fromString(id.valor)).firstResult()?.toDomain()

    override fun atualizar(usuario: Usuario) {
        val entity = UsuarioEntity.find("id", UUID.fromString(usuario.obterDados().id.valor))
            .firstResult() ?: return

        val dados = usuario.obterDados()
        entity.nome = dados.nome
        entity.email = dados.email.valor
        entity.telefone = dados.telefone.valor
        entity.senhaHash = usuario.obterSenhaHash().valor
        entity.ativo = dados.ativo
        entity.dataAtualizacao = LocalDateTime.now()
    }

    override fun desativar(id: UsuarioId) {
        val entity = UsuarioEntity.find("id", UUID.fromString(id.valor))
            .firstResult() ?: return

        entity.ativo = false
        entity.dataAtualizacao = LocalDateTime.now()
    }

    override fun existeEmailEmOutroUsuario(email: String, idAtual: UsuarioId): Boolean =
        UsuarioEntity.count(
            "email = ?1 and id <> ?2",
            email,
            UUID.fromString(idAtual.valor)
        ) > 0

    override fun buscarInativoPorCpf(cpf: String): Usuario? =
        UsuarioEntity.find("cpf = ?1 and ativo = ?2", cpf, false).firstResult()?.toDomain()
}
