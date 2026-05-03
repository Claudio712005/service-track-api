package br.com.servicetrack.infrastructure.auditoria.annotation

import jakarta.interceptor.InterceptorBinding

@InterceptorBinding
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuditavelInterceptorBinding