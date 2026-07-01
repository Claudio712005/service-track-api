package br.com.servicetrack.infrastructure.notificacao.scheduler

import br.com.servicetrack.application.notificacao.ports.`in`.ProcessarNotificacoesPendentesUseCase
import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.Scheduled.ConcurrentExecution
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class EnvioNotificacaoScheduler(
    private val processarPendentes: ProcessarNotificacoesPendentesUseCase,
) {

    private val logger: Logger = Logger.getLogger(EnvioNotificacaoScheduler::class.java)

    @Scheduled(
        every = "30s",
        delayed = "15s",
        concurrentExecution = ConcurrentExecution.SKIP,
        identity = "envio-notificacao",
    )
    fun executar() {
        runCatching {
            val resultado = processarPendentes.executar()
            if (resultado.totalProcessado > 0) {
                logger.infof(
                    "Scheduler de notificações: total=%d enviadas=%d falhas=%d",
                    resultado.totalProcessado,
                    resultado.enviadas,
                    resultado.falhas,
                )
            }
        }.onFailure { ex ->
            logger.error("Falha inesperada no scheduler de notificações", ex)
        }
    }
}

