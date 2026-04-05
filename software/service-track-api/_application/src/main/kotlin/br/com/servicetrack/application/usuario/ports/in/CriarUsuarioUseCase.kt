package br.com.servicetrack.application.usuario.ports.`in`

import br.com.servicetrack.application.usuario.dto.request.CriarUsuarioCommand
import br.com.servicetrack.application.usuario.dto.response.UsuarioResponse

interface CriarUsuarioUseCase {

    fun criarUsuario(command: CriarUsuarioCommand): UsuarioResponse
}
