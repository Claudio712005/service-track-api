package br.com.servicetrack.application.observabilidade.annotation

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Rastreavel(
    val nome: String = "",
)
