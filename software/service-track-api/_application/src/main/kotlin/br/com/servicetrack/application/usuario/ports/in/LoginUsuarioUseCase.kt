package br.com.servicetrack.application.usuario.ports.`in`

import br.com.servicetrack.application.usuario.dto.request.LoginReqDTO
import br.com.servicetrack.application.usuario.dto.response.LoginResDTO

interface LoginUsuarioUseCase {

    fun login(req: LoginReqDTO): LoginResDTO
}
