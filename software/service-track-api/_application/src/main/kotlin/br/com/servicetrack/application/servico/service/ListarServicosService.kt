package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.observabilidade.annotation.Observavel
import br.com.servicetrack.application.observabilidade.enums.CodigoUseCase
import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.application.servico.dto.ServicoResumoResDTO
import br.com.servicetrack.application.servico.ports.`in`.ListarServicosUseCase
import br.com.servicetrack.application.servico.ports.`out`.ServicoRepositoryPort

class ListarServicosService(
    private val repository: ServicoRepositoryPort
) : ListarServicosUseCase {

    @Observavel(codigo = CodigoUseCase.SERVICO_LISTAR)

    override fun listarServicos(): List<ServicoResDTO> =
        repository.listarTodos().map { ServicoResDTO.de(it) }

    @Observavel(codigo = CodigoUseCase.SERVICO_CATALOGO)

    override fun listarResumidos(): List<ServicoResumoResDTO> =
        repository.listarTodos().map { ServicoResumoResDTO.de(it) }
}
