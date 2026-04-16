package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.application.servico.ports.`in`.BuscarServicoUseCase
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId

class BuscarServicoService(
    private val repository: ServicoRepositoryPort
) : BuscarServicoUseCase {

    override fun buscarServico(id: ServicoId): ServicoResDTO {
        val servico = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Servico::class.java.name, arrayOf(id.valor))
        return ServicoResDTO.de(servico)
    }
}
