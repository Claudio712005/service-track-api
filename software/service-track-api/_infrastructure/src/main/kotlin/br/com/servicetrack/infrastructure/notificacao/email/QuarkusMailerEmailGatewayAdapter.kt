package br.com.servicetrack.infrastructure.notificacao.email

import br.com.servicetrack.application.notificacao.dto.EmailMensagem
import br.com.servicetrack.application.notificacao.dto.ResultadoEnvio
import br.com.servicetrack.application.notificacao.ports.out.EmailGatewayPort
import io.quarkus.mailer.Mail
import io.quarkus.mailer.Mailer
import io.smallrye.faulttolerance.api.ExponentialBackoff
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.faulttolerance.CircuitBreaker
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.jboss.logging.Logger

@ApplicationScoped
class QuarkusMailerEmailGatewayAdapter(
    private val mailer: Mailer,
) : EmailGatewayPort {

    private val logger: Logger = Logger.getLogger(QuarkusMailerEmailGatewayAdapter::class.java)

    @Retry(maxRetries = 3)
    @ExponentialBackoff(factor = 2, maxDelay = 10000)
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 1.0, delay = 60000, successThreshold = 2)
    @Timeout(10000)
    override fun enviar(mensagem: EmailMensagem): ResultadoEnvio {
        return try {
            val mail = Mail.withHtml(
                mensagem.destinatario.valor,
                mensagem.assunto,
                mensagem.corpoHtml,
            )
            mail.text = mensagem.corpoTexto
            if (mensagem.copias.isNotEmpty()) {
                mail.cc = mensagem.copias.map { it.valor }.toMutableList()
            }
            mailer.send(mail)
            ResultadoEnvio.Sucesso
        } catch (ex: RuntimeException) {
            logger.warnf("[Email] Falha ao enviar e-mail para %s: %s", mensagem.destinatario.valor, ex.message)
            ResultadoEnvio.Falha(ex.message ?: ex.javaClass.simpleName)
        }
    }
}
