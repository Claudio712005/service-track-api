package br.com.servicetrack.application.mecanico.ports.`in`

import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO

interface BuscarMecanicoUseCase {
    fun buscarMecanico(id: String): MecanicoResDTO
}
