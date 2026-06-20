package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.notificacao.Notificacao
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BuscarNotificacaoUseCaseImplTest {

    private val repository = mockk<NotificacaoRepositoryPort>()
    private val jwt = mockk<JwtPort>()
    private val useCase = BuscarNotificacaoUseCaseImpl(repository, jwt)

    private val usuarioId = UsuarioId.gerar()

    private fun novaNotificacao(destinatario: UsuarioId = usuarioId): Notificacao = Notificacao.gerar(
        assunto = AssuntoNotificacao("Assunto"),
        titulo = TituloNotificacao("Título"),
        descricao = DescricaoNotificacao("Descrição"),
        variaveis = VariaveisTemplate.de(mapOf("os" to "1")),
        tipoNotificacao = TipoNotificacao.EMAIL,
        tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
        destinatario = destinatario,
    )

    @Test
    fun `deve retornar notificacao quando pertence ao usuario`() {
        val notificacao = novaNotificacao()
        every { jwt.getUsuarioId() } returns usuarioId
        every { repository.buscarPorId(notificacao.id) } returns notificacao

        val resultado = useCase.executar(notificacao.id.value)

        assertEquals(notificacao.id.value, resultado.id)
        assertEquals("Título", resultado.titulo)
        assertEquals("Assunto", resultado.assunto)
    }

    @Test
    fun `deve lancar excecao quando notificacao nao encontrada`() {
        val idInexistente = UsuarioId.gerar().valor
        every { jwt.getUsuarioId() } returns usuarioId
        every { repository.buscarPorId(any()) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            useCase.executar(idInexistente)
        }
    }

    @Test
    fun `deve lancar excecao quando notificacao nao pertence ao usuario`() {
        val outroUsuario = UsuarioId.gerar()
        val notificacao = novaNotificacao(destinatario = outroUsuario)
        every { jwt.getUsuarioId() } returns usuarioId
        every { repository.buscarPorId(notificacao.id) } returns notificacao

        assertThrows<OperacaoNegadaException> {
            useCase.executar(notificacao.id.value)
        }
    }
}
