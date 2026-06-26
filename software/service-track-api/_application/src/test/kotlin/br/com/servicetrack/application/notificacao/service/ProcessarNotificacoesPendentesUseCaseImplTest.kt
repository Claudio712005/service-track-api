package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.notificacao.dto.ConteudoRenderizado
import br.com.servicetrack.application.notificacao.dto.ResultadoEnvio
import br.com.servicetrack.application.notificacao.ports.out.EmailDestinatarioResolverPort
import br.com.servicetrack.application.notificacao.ports.out.EmailGatewayPort
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.notificacao.ports.out.TemplateRendererPort
import br.com.servicetrack.application.shared.ports.out.TransactionRunnerPort
import br.com.servicetrack.domain.notificacao.Notificacao
import br.com.servicetrack.domain.notificacao.StatusEnvio
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProcessarNotificacoesPendentesUseCaseImplTest {

    private val repository = mockk<NotificacaoRepositoryPort>(relaxed = true)
    private val renderer = mockk<TemplateRendererPort>()
    private val emailGateway = mockk<EmailGatewayPort>()
    private val destinatarioResolver = mockk<EmailDestinatarioResolverPort>()
    private val transactionRunner = object : TransactionRunnerPort {
        override fun <T> executarEmNovaTransacao(block: () -> T): T = block()
    }

    private val useCase = ProcessarNotificacoesPendentesUseCaseImpl(
        repository = repository,
        renderer = renderer,
        emailGateway = emailGateway,
        destinatarioResolver = destinatarioResolver,
        transactionRunner = transactionRunner,
    )

    private fun novaNotificacao(): Notificacao = Notificacao.gerar(
        assunto = AssuntoNotificacao("Assunto"),
        titulo = TituloNotificacao("Título"),
        descricao = DescricaoNotificacao("Descrição"),
        variaveis = VariaveisTemplate.de(mapOf("os" to "1")),
        tipoNotificacao = TipoNotificacao.EMAIL,
        tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
        destinatario = UsuarioId.gerar(),
    )

    private val conteudo = ConteudoRenderizado(
        assunto = "Assunto",
        corpoHtml = "<p>html</p>",
        corpoTexto = "texto",
    )

    @Test
    fun `deve retornar lote vazio quando nao ha pendentes`() {
        every { repository.buscarPendentesParaEnvio(any()) } returns emptyList()

        val resultado = useCase.executar()

        assertEquals(0, resultado.totalProcessado)
        assertEquals(0, resultado.enviadas)
        assertEquals(0, resultado.falhas)
        verify(exactly = 0) { repository.atualizar(any()) }
    }

    @Test
    fun `deve enviar notificacao e marcar como ENVIADA em caso de sucesso`() {
        val notificacao = novaNotificacao()
        every { repository.buscarPendentesParaEnvio(any()) } returns listOf(notificacao)
        every { destinatarioResolver.resolverEmail(notificacao.destinatario) } returns Email("dest@x.com")
        every { renderer.renderizar(any(), any()) } returns conteudo
        every { emailGateway.enviar(any()) } returns ResultadoEnvio.Sucesso

        val resultado = useCase.executar()

        assertEquals(1, resultado.totalProcessado)
        assertEquals(1, resultado.enviadas)
        assertEquals(0, resultado.falhas)
        assertEquals(StatusEnvio.ENVIADA, notificacao.statusEnvio)
        verify(exactly = 1) { repository.atualizar(notificacao) }
    }

    @Test
    fun `deve manter PENDENTE quando gateway falha abaixo do limite de tentativas`() {
        val notificacao = novaNotificacao()
        every { repository.buscarPendentesParaEnvio(any()) } returns listOf(notificacao)
        every { destinatarioResolver.resolverEmail(notificacao.destinatario) } returns Email("dest@x.com")
        every { renderer.renderizar(any(), any()) } returns conteudo
        every { emailGateway.enviar(any()) } returns ResultadoEnvio.Falha("SMTP timeout")

        val resultado = useCase.executar()

        assertEquals(1, resultado.totalProcessado)
        assertEquals(0, resultado.enviadas)
        assertEquals(1, resultado.falhas)
        assertEquals(StatusEnvio.PENDENTE, notificacao.statusEnvio)
        assertEquals(1, notificacao.tentativasEnvio)
        assertEquals("SMTP timeout", notificacao.ultimoErro)
    }

    @Test
    fun `deve transitar para FALHA_ENVIO ao esgotar tentativas`() {
        val notificacao = novaNotificacao()
        notificacao.registrarTentativaFalha("erro1", maxTentativas = ProcessarNotificacoesPendentesUseCaseImpl.MAX_TENTATIVAS)
        notificacao.registrarTentativaFalha("erro2", maxTentativas = ProcessarNotificacoesPendentesUseCaseImpl.MAX_TENTATIVAS)

        every { repository.buscarPendentesParaEnvio(any()) } returns listOf(notificacao)
        every { destinatarioResolver.resolverEmail(notificacao.destinatario) } returns Email("dest@x.com")
        every { renderer.renderizar(any(), any()) } returns conteudo
        every { emailGateway.enviar(any()) } returns ResultadoEnvio.Falha("SMTP down")

        val resultado = useCase.executar()

        assertEquals(1, resultado.falhas)
        assertEquals(StatusEnvio.FALHA_ENVIO, notificacao.statusEnvio)
        assertEquals(3, notificacao.tentativasEnvio)
    }

    @Test
    fun `deve registrar falha quando renderer lanca excecao`() {
        val notificacao = novaNotificacao()
        every { repository.buscarPendentesParaEnvio(any()) } returns listOf(notificacao)
        every { destinatarioResolver.resolverEmail(notificacao.destinatario) } returns Email("dest@x.com")
        every { renderer.renderizar(any(), any()) } throws RuntimeException("template ausente")

        val resultado = useCase.executar()

        assertEquals(1, resultado.falhas)
        assertEquals(0, resultado.enviadas)
        assertEquals(1, notificacao.tentativasEnvio)
        assertEquals("template ausente", notificacao.ultimoErro)
    }

    @Test
    fun `deve processar cada item em transacao propria`() {
        val n1 = novaNotificacao()
        val n2 = novaNotificacao()
        every { repository.buscarPendentesParaEnvio(any()) } returns listOf(n1, n2)
        every { destinatarioResolver.resolverEmail(any()) } returns Email("dest@x.com")
        every { renderer.renderizar(any(), any()) } returns conteudo
        every { emailGateway.enviar(any()) } returnsMany listOf(ResultadoEnvio.Sucesso, ResultadoEnvio.Falha("x"))

        var chamadas = 0
        val runner = object : TransactionRunnerPort {
            override fun <T> executarEmNovaTransacao(block: () -> T): T {
                chamadas += 1
                return block()
            }
        }
        val useCaseLocal = ProcessarNotificacoesPendentesUseCaseImpl(
            repository, renderer, emailGateway, destinatarioResolver, runner,
        )

        val resultado = useCaseLocal.executar()

        assertEquals(2, resultado.totalProcessado)
        assertEquals(1, resultado.enviadas)
        assertEquals(1, resultado.falhas)
        assertEquals(2, chamadas)
    }
}
