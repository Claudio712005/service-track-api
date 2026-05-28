package br.com.servicetrack.infrastructure.notificacao

import br.com.servicetrack.application.notificacao.ports.out.EmailDestinatarioResolverPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class EmailDestinatarioResolverAdapter(
    private val usuarioRepository: UsuarioRepositoryPort,
) : EmailDestinatarioResolverPort {

    override fun resolverEmail(usuarioId: UsuarioId): Email {
        val usuario = usuarioRepository.buscarPorId(usuarioId)
            ?: throw DomainException("Usuário ${usuarioId.valor} não encontrado para envio de notificação")
        return usuario.obterDados().email
    }
}

