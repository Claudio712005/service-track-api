package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.application.servico.dto.ServicoResumoResDTO
import br.com.servicetrack.application.servico.ports.`in`.ListarServicosUseCase
import br.com.servicetrack.application.servico.ports.`out`.ServicoRepositoryPort

class ListarServicosService(
    private val repository: ServicoRepositoryPort
) : ListarServicosUseCase {

    override fun listarServicos(): List<ServicoResDTO> =
        repository.listarTodos().map { ServicoResDTO.de(it) }

    override fun listarResumidos(): List<ServicoResumoResDTO> =
        repository.listarTodos().map { ServicoResumoResDTO.de(it) }
}
