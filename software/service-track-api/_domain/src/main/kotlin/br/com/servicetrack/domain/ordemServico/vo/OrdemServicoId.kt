package br.com.servicetrack.domain.ordemServico.vo

import java.util.UUID

@JvmInline
value class OrdemServicoId(val valor: String) {
    companion object {
        fun gerar() = OrdemServicoId(UUID.randomUUID().toString())
    }
}
