package br.com.servicetrack.infrastructure.config.interceptor

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.domain.auditoria.Auditoria
import br.com.servicetrack.domain.auditoria.EventoAuditoria
import br.com.servicetrack.infrastructure.auditoria.annotation.AuditavelInterceptorBinding
import br.com.servicetrack.infrastructure.auditoria.persistence.AuditoriaRepositoryAdapter
import jakarta.interceptor.AroundInvoke
import jakarta.interceptor.Interceptor
import jakarta.interceptor.InvocationContext

@AuditavelInterceptorBinding
@Interceptor
class AuditoriaInterceptor(
    private val geradorAuditoria: GeradorAuditoria,
    private val auditoriaRepository: AuditoriaRepositoryAdapter
) {

    @AroundInvoke
    fun interceptar(ctx: InvocationContext): Any? {

        val method = ctx.method

        val auditavel = method.getAnnotation(
            Auditavel::class.java
        ) ?: return ctx.proceed()

        val resultado = ctx.proceed()

        val dados = geradorAuditoria.gerar(null, resultado)

        val auditoria = Auditoria.registrar(
            referenciaId = extrairReferenciaId(resultado),
            eventoAuditoria = EventoAuditoria.criacao(auditavel.entidade),
            dados = dados,
            contexto = contextoAtual()
        )

        auditoriaRepository.salvar(auditoria)

        return resultado
    }
}