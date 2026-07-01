package br.com.servicetrack.domain.notificacao.vo

import br.com.servicetrack.domain.shared.exception.DomainException

class VariaveisTemplate private constructor(private val valores: Map<String, String>) {

    fun comoMap(): Map<String, String> = valores

    operator fun get(chave: String): String? = valores[chave]

    fun estaVazio(): Boolean = valores.isEmpty()

    override fun equals(other: Any?): Boolean =
        this === other || (other is VariaveisTemplate && valores == other.valores)

    override fun hashCode(): Int = valores.hashCode()

    override fun toString(): String = "VariaveisTemplate(chaves=${valores.keys})"

    companion object {
        val VAZIO: VariaveisTemplate = VariaveisTemplate(emptyMap())

        fun de(valores: Map<String, String>): VariaveisTemplate {
            valores.keys.forEach { chave ->
                if (chave.isBlank()) {
                    throw DomainException("Chave de variável de template não pode ser vazia")
                }
            }
            return VariaveisTemplate(valores.toMap())
        }
    }
}

