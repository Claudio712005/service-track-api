package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.ordemServico.ports.out.ItemOrdemServicoRepositoryPort
import br.com.servicetrack.application.servico.dto.TempoMedioResDTO
import br.com.servicetrack.application.servico.ports.`in`.BuscarTempoMedioConclusaoUseCase
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.domain.servico.UnidadeTempoEnum
import br.com.servicetrack.domain.servico.vo.ServicoId
import java.time.Duration

class BuscarTempoMedioConclusaoService(
    private val servicoRepository: ServicoRepositoryPort,
    private val itemOrdemServicoRepository: ItemOrdemServicoRepositoryPort,
) : BuscarTempoMedioConclusaoUseCase {

    override fun buscarTempoMedio(servicoId: String, unidade: UnidadeTempoEnum): TempoMedioResDTO {
        val id = ServicoId(servicoId)
        servicoRepository.buscarPorId(id) ?: throw EntidadeNaoEncontradaException("Serviço", arrayOf(servicoId))

        val itens = itemOrdemServicoRepository.buscarItensConcluidos(id)

        if (itens.isEmpty()) {
            return TempoMedioResDTO(servicoId = servicoId, tempoMedio = 0.0, unidade = unidade, totalAmostras = 0)
        }

        val totalSegundos = itens
            .mapNotNull { item -> item.dataRealizacao?.let { Duration.between(item.dataCriacao, it).seconds } }
            .sum()

        val mediaSegundos = totalSegundos.toDouble() / itens.size

        val tempoMedio = when (unidade) {
            UnidadeTempoEnum.SEGUNDOS -> mediaSegundos
            UnidadeTempoEnum.MINUTOS -> mediaSegundos / 60.0
            UnidadeTempoEnum.HORAS -> mediaSegundos / 3600.0
            UnidadeTempoEnum.DIAS -> mediaSegundos / 86400.0
        }

        return TempoMedioResDTO(
            servicoId = servicoId,
            tempoMedio = tempoMedio,
            unidade = unidade,
            totalAmostras = itens.size,
        )
    }
}
