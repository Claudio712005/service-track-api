package br.com.servicetrack.application.ordemServico.service.support

import br.com.servicetrack.application.exception.LinkDecisaoInvalidoException
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.ports.out.TokenDecisaoOrcamentoPort
import br.com.servicetrack.domain.ordemServico.OrdemServico

class ResolvedorOrdemServicoPorToken(
    private val tokenPort: TokenDecisaoOrcamentoPort,
    private val osRepository: OrdemServicoRepositoryPort,
) {

    fun resolver(token: String): OrdemServico {
        val claims = tokenPort.validar(token)
            ?: throw LinkDecisaoInvalidoException()

        val os = osRepository.buscarPorId(claims.ordemServicoId)
            ?: throw LinkDecisaoInvalidoException("Ordem de serviço não encontrada")

        if (os.clienteId != claims.clienteId) {
            throw LinkDecisaoInvalidoException("Este link não pertence ao titular da ordem de serviço")
        }

        return os
    }
}
