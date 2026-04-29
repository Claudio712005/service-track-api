package br.com.servicetrack.application.usuario.ports.`in`

import br.com.servicetrack.application.usuario.dto.request.AtualizarUsuarioReqDTO
import br.com.servicetrack.application.usuario.dto.response.ClienteResDTO
import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface AtualizarUsuarioUseCase {
    fun atualizarUsuario(id: UsuarioId, req: AtualizarUsuarioReqDTO): ClienteResDTO
}
