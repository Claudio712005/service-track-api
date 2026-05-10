package br.com.servicetrack.application.usuario.ports.`in`

import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface DesativarUsuarioUseCase {
    fun desativarUsuario(id: UsuarioId)
}
