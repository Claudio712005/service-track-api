package br.com.servicetrack.domain.auditoria.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class EnderecoIp private constructor(val value: String) {

    companion object {
        fun criar(endereco: String): EnderecoIp {
            val regex = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$")
            if(!regex.matches(endereco)) { throw DomainException("Endereço IP inválido: $endereco") }
            return EnderecoIp(endereco)
        }
    }
}
