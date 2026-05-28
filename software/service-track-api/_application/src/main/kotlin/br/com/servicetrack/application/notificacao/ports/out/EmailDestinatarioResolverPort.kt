package br.com.servicetrack.application.notificacao.ports.out

import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface EmailDestinatarioResolverPort {

    fun resolverEmail(usuarioId: UsuarioId): Email
}

