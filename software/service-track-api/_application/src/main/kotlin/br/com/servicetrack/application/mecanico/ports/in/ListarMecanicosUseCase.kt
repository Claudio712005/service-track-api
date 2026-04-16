package br.com.servicetrack.application.mecanico.ports.`in`

import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO

interface ListarMecanicosUseCase {
    fun listarMecanicos(): List<MecanicoResDTO>
}
