package br.com.servicetrack.application.observabilidade.annotation

import br.com.servicetrack.application.observabilidade.enums.CodigoUseCase

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Observavel(
    val codigo: CodigoUseCase,
)
