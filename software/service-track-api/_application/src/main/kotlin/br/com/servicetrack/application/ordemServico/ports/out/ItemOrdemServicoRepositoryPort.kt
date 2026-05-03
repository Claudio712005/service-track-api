package br.com.servicetrack.application.ordemServico.ports.out

import br.com.servicetrack.domain.ordemServico.ItemOrdemServico
import br.com.servicetrack.domain.servico.vo.ServicoId

interface ItemOrdemServicoRepositoryPort {
    fun buscarItensConcluidos(servicoId: ServicoId): List<ItemOrdemServico>
}
