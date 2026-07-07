package br.com.servicetrack.application.ordemServico.ports.out

import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface AprovacaoOrcamentoLinkPort {

    fun gerarLinks(ordemServicoId: OrdemServicoId, clienteId: UsuarioId): LinksDecisaoOrcamento
}

data class LinksDecisaoOrcamento(
    val aprovarUrl: String,
    val reprovarUrl: String,
)
