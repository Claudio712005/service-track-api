package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.observabilidade.annotation.Observavel
import br.com.servicetrack.application.observabilidade.enums.CodigoUseCase
import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.application.servico.ports.`in`.BuscarServicoUseCase
import br.com.servicetrack.application.servico.ports.`out`.ServicoRepositoryPort
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId

class BuscarServicoService(
    private val repository: ServicoRepositoryPort
) : BuscarServicoUseCase {

    @Observavel(codigo = CodigoUseCase.SERVICO_BUSCAR)

    override fun buscarServico(id: ServicoId): ServicoResDTO {
        val servico = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Servico::class.java.name, arrayOf(id.valor))
        return ServicoResDTO.de(servico)
    }
}
