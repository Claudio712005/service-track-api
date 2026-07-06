package br.com.servicetrack.application.ordemServico.ports.out

import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface TokenDecisaoOrcamentoPort {

    fun gerar(ordemServicoId: OrdemServicoId, clienteId: UsuarioId): String

    fun validar(token: String): DecisaoOrcamentoTokenClaims?
}

data class DecisaoOrcamentoTokenClaims(
    val ordemServicoId: OrdemServicoId,
    val clienteId: UsuarioId,
)
