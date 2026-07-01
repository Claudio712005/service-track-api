package br.com.servicetrack.domain.notificacao.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class DescricaoNotificacao(val valor: String) {
    init {
        if (valor.isBlank()) {
            throw DomainException("Descrição da notificação não pode ser vazia")
        }
        if (valor.length > TAMANHO_MAXIMO) {
            throw DomainException(
                "Descrição da notificação deve ter no máximo $TAMANHO_MAXIMO caracteres",
            )
        }
    }

    companion object {
        const val TAMANHO_MAXIMO = 2000
    }
}

