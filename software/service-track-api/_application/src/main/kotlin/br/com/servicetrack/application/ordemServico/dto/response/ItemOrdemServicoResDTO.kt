package br.com.servicetrack.application.ordemServico.dto.response

import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import br.com.servicetrack.domain.ordemServico.ItemOrdemServico
import java.math.BigDecimal
import java.time.LocalDateTime

data class ItemOrdemServicoResDTO(
    @field:Rastreavel
    val id: String,
    @field:Rastreavel
    val servicoId: String,
    val valor: BigDecimal,
    val feito: Boolean,
    val mecanicoResponsavelId: String?,
    val dataRealizacao: LocalDateTime?,
    val observacao: String?,
) {
    companion object {
        fun de(domain: ItemOrdemServico) = ItemOrdemServicoResDTO(
            id = domain.id.valor,
            servicoId = domain.servicoId.valor,
            valor = domain.valor.valor,
            feito = domain.feito,
            mecanicoResponsavelId = domain.mecanicoResponsavelId?.valor,
            dataRealizacao = domain.dataRealizacao,
            observacao = domain.observacao,
        )
    }
}
