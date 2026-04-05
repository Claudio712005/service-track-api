package br.com.servicetrack.domain.usuario.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class Senha private constructor(val valor: String) {

    companion object {

        fun criar(senhaTextoPlano: String): Senha {
            if (senhaTextoPlano.length < 6) {
                throw DomainException("A senha deve ter no mínimo 6 caracteres")
            }
            if (!senhaTextoPlano.any { it.isUpperCase() }) {
                throw DomainException("A senha deve conter ao menos uma letra maiúscula")
            }
            if (!senhaTextoPlano.any { it.isLowerCase() }) {
                throw DomainException("A senha deve conter ao menos uma letra minúscula")
            }
            if (!senhaTextoPlano.any { it.isDigit() }) {
                throw DomainException("A senha deve conter ao menos um número")
            }
            if (!senhaTextoPlano.any { "!@#\$%^&*()".contains(it) }) {
                throw DomainException("A senha deve conter ao menos um caractere especial")
            }
            return Senha(senhaTextoPlano)
        }

        fun deHash(hash: String): Senha {
            require(hash.isNotBlank()) { "Hash da senha não pode ser vazio" }
            return Senha(hash)
        }
    }
}
