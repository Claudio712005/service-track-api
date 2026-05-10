package br.com.servicetrack.application.servico.ports.`in`

import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.application.servico.dto.ServicoResumoResDTO

interface ListarServicosUseCase {
    fun listarServicos(): List<ServicoResDTO>
    fun listarResumidos(): List<ServicoResumoResDTO>
}
