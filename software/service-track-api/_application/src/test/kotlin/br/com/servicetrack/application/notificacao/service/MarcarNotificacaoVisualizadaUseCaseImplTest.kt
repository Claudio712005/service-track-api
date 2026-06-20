package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.notificacao.Notificacao
import br.com.servicetrack.domain.notificacao.StatusEnvio
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MarcarNotificacaoVisualizadaUseCaseImplTest {

    private val repository = mockk<NotificacaoRepositoryPort>(relaxed = true)
    private val jwt = mockk<JwtPort>()
    private val useCase = MarcarNotificacaoVisualizadaUseCaseImpl(repository, jwt)

    private val usuarioId = UsuarioId.gerar()

    private fun novaNotificacaoEnviada(destinatario: UsuarioId = usuarioId): Notificacao {
        val n = Notificacao.gerar(
            assunto = AssuntoNotificacao("Assunto"),
            titulo = TituloNotificacao("Título"),
            descricao = DescricaoNotificacao("Descrição"),
            variaveis = VariaveisTemplate.de(mapOf("os" to "1")),
            tipoNotificacao = TipoNotificacao.EMAIL,
            tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
            destinatario = destinatario,
        )
        n.marcarComoEnviada()
        return n
    }

    @Test
    fun `deve marcar notificacao como visualizada com sucesso`() {
        val notificacao = novaNotificacaoEnviada()
        every { jwt.getUsuarioId() } returns usuarioId
        every { repository.buscarPorId(notificacao.id) } returns notificacao

        useCase.executar(notificacao.id)

        assertTrue(notificacao.visualizada)
        verify(exactly = 1) { repository.atualizar(notificacao) }
    }

    @Test
    fun `deve lancar excecao quando notificacao nao encontrada`() {
        val id = NotificacaoId.gerar()
        every { jwt.getUsuarioId() } returns usuarioId
        every { repository.buscarPorId(id) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            useCase.executar(id)
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar excecao quando notificacao nao pertence ao usuario`() {
        val outroUsuario = UsuarioId.gerar()
        val notificacao = novaNotificacaoEnviada(destinatario = outroUsuario)
        every { jwt.getUsuarioId() } returns usuarioId
        every { repository.buscarPorId(notificacao.id) } returns notificacao

        assertThrows<OperacaoNegadaException> {
            useCase.executar(notificacao.id)
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }
}
