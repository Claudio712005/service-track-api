package br.com.servicetrack.application.observabilidade.annotation

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mascarado(
    val nome: String = "",
    val visiveis: Int = 2,
)
