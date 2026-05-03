package br.com.servicetrack.application.mecanico.ports.`in`

import br.com.servicetrack.application.mecanico.dto.request.CadastrarMecanicoReqDTO
import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO

interface CadastrarMecanicoUseCase {

    fun cadastrarMecanico(req: CadastrarMecanicoReqDTO): MecanicoResDTO
}
