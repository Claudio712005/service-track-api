package br.com.servicetrack.application.mecanico.ports.`in`

import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO
import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface PromoverMecanicoUseCase {

    fun promoverMecanico(idMecanico: UsuarioId): MecanicoResDTO
}