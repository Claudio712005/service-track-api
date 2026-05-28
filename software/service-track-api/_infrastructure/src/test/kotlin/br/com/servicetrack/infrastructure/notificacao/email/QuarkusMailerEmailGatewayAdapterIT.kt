package br.com.servicetrack.infrastructure.notificacao.email

import br.com.servicetrack.application.notificacao.dto.EmailMensagem
import br.com.servicetrack.application.notificacao.dto.ResultadoEnvio
import br.com.servicetrack.domain.usuario.vo.Email
import io.quarkus.mailer.MockMailbox
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class QuarkusMailerEmailGatewayAdapterIT {

    @Inject
    lateinit var gateway: QuarkusMailerEmailGatewayAdapter

    @Inject
    lateinit var mockMailbox: MockMailbox

    @BeforeEach
    fun limpar() {
        mockMailbox.clear()
    }

    @Test
    fun `deve enviar e-mail com html texto e copias`() {
        val mensagem = EmailMensagem(
            destinatario = Email("dest@x.com"),
            copias = listOf(Email("copia1@x.com"), Email("copia2@x.com")),
            assunto = "Assunto",
            corpoHtml = "<p>HTML</p>",
            corpoTexto = "Texto",
        )

        val resultado = gateway.enviar(mensagem)

        assertTrue(resultado is ResultadoEnvio.Sucesso)
        val recebidos = mockMailbox.getMessagesSentTo("dest@x.com")
        assertEquals(1, recebidos.size)
        val mail = recebidos.first()
        assertEquals("Assunto", mail.subject)
        assertEquals("<p>HTML</p>", mail.html)
        assertEquals("Texto", mail.text)
        assertEquals(listOf("copia1@x.com", "copia2@x.com"), mail.cc)
    }

    @Test
    fun `deve enviar sem copias quando lista vazia`() {
        val mensagem = EmailMensagem(
            destinatario = Email("solo@x.com"),
            copias = emptyList(),
            assunto = "Solo",
            corpoHtml = "<p>html</p>",
            corpoTexto = "txt",
        )

        val resultado = gateway.enviar(mensagem)

        assertTrue(resultado is ResultadoEnvio.Sucesso)
        val recebidos = mockMailbox.getMessagesSentTo("solo@x.com")
        assertEquals(1, recebidos.size)
        val cc = recebidos.first().cc
        assertTrue(cc == null || cc.isEmpty())
    }
}
