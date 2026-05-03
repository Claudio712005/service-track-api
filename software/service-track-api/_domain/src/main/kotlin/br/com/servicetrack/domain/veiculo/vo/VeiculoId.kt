package br.com.servicetrack.domain.veiculo.vo

import java.util.UUID

@JvmInline
value class VeiculoId(val valor: String) {
    companion object {
        fun gerar() = VeiculoId(UUID.randomUUID().toString())
    }
}