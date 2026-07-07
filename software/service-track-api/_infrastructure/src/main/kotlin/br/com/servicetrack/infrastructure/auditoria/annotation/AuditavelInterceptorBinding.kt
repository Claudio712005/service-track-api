package br.com.servicetrack.infrastructure.auditoria.annotation

import jakarta.interceptor.InterceptorBinding

@InterceptorBinding
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Deprecated("Usar AuditoriaProxy.envolver() com antesProvider", level = DeprecationLevel.WARNING)
annotation class AuditavelInterceptorBinding
