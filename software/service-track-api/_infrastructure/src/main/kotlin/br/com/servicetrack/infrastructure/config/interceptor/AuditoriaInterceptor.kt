package br.com.servicetrack.infrastructure.config.interceptor

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.infrastructure.auditoria.annotation.AuditavelInterceptorBinding
import jakarta.inject.Inject
import jakarta.interceptor.AroundInvoke
import jakarta.interceptor.Interceptor
import jakarta.interceptor.InvocationContext

@AuditavelInterceptorBinding
@Interceptor
class AuditoriaInterceptor {

    @Inject
    lateinit var auditoriaPort: RegistrarAuditoriaPort

    @AroundInvoke
    fun interceptar(ctx: InvocationContext): Any? {
        val auditavel = ctx.method.getAnnotation(Auditavel::class.java)
            ?: return ctx.proceed()

        val resultado = ctx.proceed()

        runCatching {
            val antes = AuditoriaContextoHolder.obterAntes()
            val referenciaId = extrairId(resultado)
                ?: extrairId(ctx.parameters?.firstOrNull())
                ?: return@runCatching
            auditoriaPort.registrar(
                entidade = auditavel.entidade,
                evento = auditavel.evento,
                referenciaId = referenciaId,
                antes = antes,
                depois = resultado,
            )
        }.also { AuditoriaContextoHolder.limpar() }

        return resultado
    }

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
