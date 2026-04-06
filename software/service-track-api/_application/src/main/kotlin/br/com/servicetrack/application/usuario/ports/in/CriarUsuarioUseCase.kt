package br.com.servicetrack.application.usuario.ports.`in`

import br.com.servicetrack.application.usuario.dto.request.CadastrarClienteReqDTO
import br.com.servicetrack.application.usuario.dto.response.ClienteResDTO

interface CriarUsuarioUseCase {

    fun criarUsuario(req: CadastrarClienteReqDTO): ClienteResDTO
}
