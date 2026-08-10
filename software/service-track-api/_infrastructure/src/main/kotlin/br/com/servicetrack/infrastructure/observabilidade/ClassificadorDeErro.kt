package br.com.servicetrack.infrastructure.observabilidade

import java.lang.reflect.InvocationTargetException

object ClassificadorDeErro {

    private val PACOTES_CURADOS = listOf(
        "br.com.servicetrack.application.exception",
        "br.com.servicetrack.domain",
    )

    data class Classificacao(
        val curado: Boolean,
        val tipo: String,
        val mensagem: String?,
    )

    fun classificar(erro: Throwable): Classificacao {
        val raiz = desembrulhar(erro)
        val tipo = raiz.javaClass.simpleName
        val curado = PACOTES_CURADOS.any { raiz.javaClass.name.startsWith(it) }
        return Classificacao(
            curado = curado,
            tipo = tipo,
            mensagem = if (curado) raiz.message else null,
        )
    }

    fun desembrulhar(erro: Throwable): Throwable =
        if (erro is InvocationTargetException) erro.cause ?: erro else erro
}
