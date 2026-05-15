package br.com.servicetrack.application.dashboard.ports.`in`

import br.com.servicetrack.application.dashboard.dto.response.ResumoDashClienteResDTO

interface BuscarResumoDashClienteUseCase {

    fun buscarResumo(clienteId: String): ResumoDashClienteResDTO
}
