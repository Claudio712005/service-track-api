package br.com.servicetrack.infrastructure.usuario.persistence

import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.usuario.Usuario
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class UsuarioRepositoryAdapter : UsuarioRepositoryPort {

    override fun salvar(usuario: Usuario) {
        UsuarioEntity.de(usuario).persist()
    }

    override fun existePorEmailOuCpf(email: String, cpf: String): Boolean =
        UsuarioEntity.count("email = ?1 or cpf = ?2", email, cpf) > 0

    override fun buscarPorEmail(email: String): Usuario? =
        UsuarioEntity.find("email", email).firstResult()?.toDomain()
}
