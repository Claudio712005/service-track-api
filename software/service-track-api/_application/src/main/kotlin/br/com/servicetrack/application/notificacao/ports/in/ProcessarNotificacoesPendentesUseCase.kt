package br.com.servicetrack.application.notificacao.ports.`in`

interface ProcessarNotificacoesPendentesUseCase {

    fun executar(): ResultadoLote

    data class ResultadoLote(
        val totalProcessado: Int,
        val enviadas: Int,
        val falhas: Int,
    )
}

