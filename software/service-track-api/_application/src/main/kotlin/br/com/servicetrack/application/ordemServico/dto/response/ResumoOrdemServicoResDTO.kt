package br.com.servicetrack.application.ordemServico.dto.response

import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum

data class ResumoOrdemServicoResDTO(
    @field:Rastreavel
    val id: String,
    @field:Rastreavel
    val mecanicoId: String,
    @field:Rastreavel
    val clienteId: String,
    val observacao: String,
    val motivo: String,
    @field:Rastreavel
    val status: StatusOrdemServicoEnum
) {
    companion object {
        fun de(domain: OrdemServico): ResumoOrdemServicoResDTO {
            return ResumoOrdemServicoResDTO(
                id = domain.id.valor,
                mecanicoId = domain.obterMecanicoId().valor,
                clienteId = domain.clienteId.valor,
                observacao = domain.observacao,
                motivo = domain.motivo,
                status = domain.obterStatus()
            )
        }
    }
}
