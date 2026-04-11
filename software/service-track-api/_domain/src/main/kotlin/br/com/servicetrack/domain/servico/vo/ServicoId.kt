package br.com.servicetrack.domain.servico.vo

import java.util.UUID

@JvmInline
value class ServicoId (val value: String) {
    companion object {
        fun gerar() = ServicoId(UUID.randomUUID().toString())
    }
}