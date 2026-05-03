package br.com.servicetrack.application.mecanico.ports.`in`

import br.com.servicetrack.application.mecanico.dto.request.AtualizarMecanicoReqDTO
import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO

interface AtualizarMecanicoUseCase {

    fun atualizarMecanico(id: String, req: AtualizarMecanicoReqDTO): MecanicoResDTO
}