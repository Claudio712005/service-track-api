package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.notificacao.dto.FiltroNotificacaoDTO
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.shared.dto.PageResDTO
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
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ListarNotificacoesUseCaseImplTest {

    private val repository = mockk<NotificacaoRepositoryPort>()
    private val jwt = mockk<JwtPort>()
    private val useCase = ListarNotificacoesUseCaseImpl(repository, jwt)

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
    fun `deve listar notificacoes filtrando por destinatario do jwt`() {
        every { jwt.getUsuarioId() } returns usuarioId
        val notificacao = novaNotificacao()
        val filtroCapturado = slot<FiltroNotificacaoDTO>()
        every { repository.listar(capture(filtroCapturado)) } returns PageResDTO.de(
            content = listOf(notificacao),
            page = 0,
            size = 20,
            total = 1,
        )

        val resultado = useCase.executar(null, 0, 20)

        assertEquals(1, resultado.content.size)
        assertEquals(notificacao.id.value, resultado.content[0].id)
        assertEquals(usuarioId.valor, filtroCapturado.captured.destinatarioId)
    }

    @Test
    fun `deve passar filtro de visualizada ao repositorio`() {
        every { jwt.getUsuarioId() } returns usuarioId
        val filtroCapturado = slot<FiltroNotificacaoDTO>()
        every { repository.listar(capture(filtroCapturado)) } returns PageResDTO.de(
            content = emptyList(),
            page = 0,
            size = 20,
            total = 0,
        )

        useCase.executar(false, 0, 20)

        assertEquals(false, filtroCapturado.captured.visualizada)
    }

    @Test
    fun `deve retornar pagina vazia quando nao ha notificacoes`() {
        every { jwt.getUsuarioId() } returns usuarioId
        every { repository.listar(any()) } returns PageResDTO.de(
            content = emptyList(),
            page = 0,
            size = 20,
            total = 0,
        )

        val resultado = useCase.executar(null, 0, 20)

        assertEquals(0, resultado.content.size)
        assertEquals(0, resultado.total)
    }

    @Test
    fun `deve respeitar parametros de paginacao`() {
        every { jwt.getUsuarioId() } returns usuarioId
        val filtroCapturado = slot<FiltroNotificacaoDTO>()
        every { repository.listar(capture(filtroCapturado)) } returns PageResDTO.de(
            content = emptyList(),
            page = 2,
            size = 10,
            total = 0,
        )

        useCase.executar(null, 2, 10)

        assertEquals(2, filtroCapturado.captured.page)
        assertEquals(10, filtroCapturado.captured.size)
    }
}
