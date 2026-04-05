package br.com.servicetrack.infrastructure.criptografia

import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import jakarta.enterprise.context.ApplicationScoped
import org.mindrot.jbcrypt.BCrypt

@ApplicationScoped
class BcryptCriptografiaAdapter : CriptografiaPort {

    override fun criptografar(senha: String): String = BCrypt.hashpw(senha, BCrypt.gensalt())

    override fun comparar(senhaHash: String, senha: String): Boolean = BCrypt.checkpw(senha, senhaHash)
}
