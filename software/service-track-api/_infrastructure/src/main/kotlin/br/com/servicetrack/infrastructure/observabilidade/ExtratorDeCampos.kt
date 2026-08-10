package br.com.servicetrack.infrastructure.observabilidade

import br.com.servicetrack.application.observabilidade.annotation.Mascarado
import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import java.lang.reflect.Field
import java.lang.reflect.Method

object ExtratorDeCampos {

    private const val OCULTO = "***"

    fun extrair(metodo: Method, argumentos: Array<Any?>, resultado: Any?): Map<String, String> {
        val campos = LinkedHashMap<String, String>()
        campos.putAll(dosParametros(metodo, argumentos))
        argumentos.forEach { campos.putAll(dosCampos(it)) }
        campos.putAll(dosCampos(resultado))
        return campos
    }

    fun mascarar(valor: String, visiveis: Int): String {
        if (visiveis <= 0) return OCULTO
        if (valor.length <= visiveis) return OCULTO
        return OCULTO + valor.takeLast(visiveis)
    }

    private fun dosParametros(metodo: Method, argumentos: Array<Any?>): Map<String, String> {
        val campos = LinkedHashMap<String, String>()
        metodo.parameterAnnotations.forEachIndexed { indice, anotacoes ->
            val valor = argumentos.getOrNull(indice) ?: return@forEachIndexed
            anotacoes.forEach { anotacao ->
                when (anotacao) {
                    is Rastreavel -> nomear(anotacao.nome, "arg$indice")
                        .let { campos[it] = texto(valor) }

                    is Mascarado -> nomear(anotacao.nome, "arg$indice")
                        .let { campos[it] = mascarar(texto(valor), anotacao.visiveis) }
                }
            }
        }
        return campos
    }

    private fun dosCampos(alvo: Any?): Map<String, String> {
        if (alvo == null || naoInspecionavel(alvo)) return emptyMap()
        val campos = LinkedHashMap<String, String>()
        alvo.javaClass.declaredFields.forEach { campo ->
            val rastreavel = campo.getAnnotation(Rastreavel::class.java)
            val mascarado = campo.getAnnotation(Mascarado::class.java)
            if (rastreavel == null && mascarado == null) return@forEach
            val valor = leitura(campo, alvo) ?: return@forEach
            if (rastreavel != null) {
                campos[nomear(rastreavel.nome, campo.name)] = valor
            } else if (mascarado != null) {
                campos[nomear(mascarado.nome, campo.name)] = mascarar(valor, mascarado.visiveis)
            }
        }
        return campos
    }

    private fun naoInspecionavel(alvo: Any): Boolean {
        val pacote = alvo.javaClass.packageName
        return pacote.startsWith("java.") || pacote.startsWith("kotlin.")
    }

    private fun leitura(campo: Field, alvo: Any): String? = runCatching {
        campo.isAccessible = true
        campo.get(alvo)?.let { texto(it) }
    }.getOrNull()

    private fun texto(valor: Any): String = runCatching {
        val interno = valor.javaClass.getDeclaredField("valor")
        interno.isAccessible = true
        interno.get(valor)?.toString()
    }.getOrNull() ?: valor.toString()

    private fun nomear(informado: String, padrao: String): String =
        informado.ifBlank { padrao }
}
