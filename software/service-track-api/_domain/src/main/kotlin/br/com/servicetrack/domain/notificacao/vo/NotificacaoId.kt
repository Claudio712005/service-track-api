package br.com.servicetrack.domain.notificacao.vo

import java.util.UUID

@JvmInline
value class NotificacaoId private constructor(val value: String) {
    companion object {
        fun gerar() = NotificacaoId(UUID.randomUUID().toString())
        fun de(id: UUID) = NotificacaoId(id.toString())
    }
}
