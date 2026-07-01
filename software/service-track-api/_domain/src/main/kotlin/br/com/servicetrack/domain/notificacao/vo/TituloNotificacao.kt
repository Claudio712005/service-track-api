package br.com.servicetrack.domain.notificacao.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class TituloNotificacao(val valor: String) {
    init {
        if (valor.isBlank()) {
            throw DomainException("Título da notificação não pode ser vazio")
        }
        if (valor.length > TAMANHO_MAXIMO) {
            throw DomainException("Título da notificação deve ter no máximo $TAMANHO_MAXIMO caracteres")
        }
    }

    companion object {
        const val TAMANHO_MAXIMO = 200
    }
}

