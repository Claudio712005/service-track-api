package br.com.servicetrack.application.usuario.ports.`in`

import br.com.servicetrack.application.usuario.dto.request.ResetarSenhaReqDTO

interface ResetarSenhaUseCase {
    fun resetarSenha(req: ResetarSenhaReqDTO)
}
