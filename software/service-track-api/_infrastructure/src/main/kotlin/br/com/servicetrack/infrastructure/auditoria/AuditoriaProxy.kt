package br.com.servicetrack.infrastructure.auditoria

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import java.lang.reflect.Proxy

object AuditoriaProxy {

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> envolver(
        instancia: T,
        contrato: Class<T>,
        auditoriaPort: RegistrarAuditoriaPort,
    ): T = Proxy.newProxyInstance(
        contrato.classLoader,
        arrayOf(contrato),
    ) { _, metodo, args ->
        val resultado = try {
            metodo.invoke(instancia, *(args ?: emptyArray()))
        } catch (e: java.lang.reflect.InvocationTargetException) {
            AuditoriaContextoHolder.limpar()
            throw e.cause ?: e
        } catch (e: Exception) {
            AuditoriaContextoHolder.limpar()
            throw e
        }

        runCatching {
            val metodoImpl = instancia.javaClass.getMethod(metodo.name, *metodo.parameterTypes)
            val auditavel = metodoImpl.getAnnotation(Auditavel::class.java) ?: return@runCatching
            val antes = AuditoriaContextoHolder.obterAntes()
            val referenciaId = extrairId(resultado)
                ?: extrairId(args?.firstOrNull())
                ?: return@runCatching

            auditoriaPort.registrar(
                entidade = auditavel.entidade,
                evento = auditavel.evento,
                referenciaId = referenciaId,
                antes = antes,
                depois = resultado,
            )
        }.also { AuditoriaContextoHolder.limpar() }

        resultado
    } as T

    private fun extrairId(obj: Any?): String? {
        if (obj == null) return null
        if (obj is String) return obj
        return runCatching {
            listOf("id", "usuarioId", "valor").firstNotNullOfOrNull { fieldName ->
                runCatching {
                    val field = obj.javaClass.getDeclaredField(fieldName)
                    field.isAccessible = true
                    field.get(obj)?.toString()
                }.getOrNull()
            }
        }.getOrNull()
    }
}
