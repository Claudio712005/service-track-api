package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContarNotificacoesNaoLidasUseCaseImplTest {

    private val repository = mockk<NotificacaoRepositoryPort>()
    private val jwt = mockk<JwtPort>()
    private val useCase = ContarNotificacoesNaoLidasUseCaseImpl(repository, jwt)

    private val usuarioId = UsuarioId.gerar()

    @Test
    fun `deve retornar contagem de nao lidas do usuario autenticado`() {
        every { jwt.getUsuarioId() } returns usuarioId
        every { repository.contarNaoLidas(usuarioId) } returns 5L

        val resultado = useCase.executar()

        assertEquals(5L, resultado.total)
    }

    @Test
    fun `deve retornar zero quando nao ha notificacoes nao lidas`() {
        every { jwt.getUsuarioId() } returns usuarioId
        every { repository.contarNaoLidas(usuarioId) } returns 0L

        val resultado = useCase.executar()

        assertEquals(0L, resultado.total)
    }
}
