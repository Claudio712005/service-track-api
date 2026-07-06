package br.com.servicetrack.infrastructure.ordemServico.decisao

import br.com.servicetrack.application.ordemServico.ports.out.AprovacaoOrcamentoLinkPort
import br.com.servicetrack.application.ordemServico.ports.out.LinksDecisaoOrcamento
import br.com.servicetrack.application.ordemServico.ports.out.TokenDecisaoOrcamentoPort
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class AprovacaoOrcamentoLinkAdapter(
    @ConfigProperty(name = "servicetrack.api.base-url", defaultValue = "http://localhost:8080")
    private val baseUrl: String,
    private val tokenPort: TokenDecisaoOrcamentoPort,
) : AprovacaoOrcamentoLinkPort {

    override fun gerarLinks(ordemServicoId: OrdemServicoId, clienteId: UsuarioId): LinksDecisaoOrcamento {
        val token = tokenPort.gerar(ordemServicoId, clienteId)
        val base = baseUrl.trimEnd('/')
        return LinksDecisaoOrcamento(
            aprovarUrl = "$base/ordem-servico/orcamento/aprovacao?token=$token",
            reprovarUrl = "$base/ordem-servico/orcamento/reprovacao?token=$token",
        )
    }
}
