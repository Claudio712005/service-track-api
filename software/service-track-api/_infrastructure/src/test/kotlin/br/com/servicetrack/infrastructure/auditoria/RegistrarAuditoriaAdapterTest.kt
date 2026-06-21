package br.com.servicetrack.infrastructure.auditoria

import br.com.servicetrack.application.auditoria.dto.AuditoriaContextoDTO
import br.com.servicetrack.application.auditoria.ports.out.AuditoriaRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.auditoria.Auditoria
import br.com.servicetrack.domain.auditoria.CampoAlterado
import br.com.servicetrack.domain.auditoria.EventoAuditoria
import br.com.servicetrack.domain.auditoria.enums.TipoDadoAuditoria
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.auditoria.vo.DadosAuditoria
import br.com.servicetrack.domain.auditoria.vo.EnderecoIp
import br.com.servicetrack.domain.auditoria.vo.ReferenciaId
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.infrastructure.auditoria.factory.AuditoriaStrategyFactory
import br.com.servicetrack.infrastructure.auditoria.strategy.AuditoriaStrategy
import io.mockk.CapturingSlot
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.net.SocketAddress
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RegistrarAuditoriaAdapterTest {

    private val jwtPort = mockk<JwtPort>()
    private val strategyFactory = mockk<AuditoriaStrategyFactory>()
    private val repository = mockk<AuditoriaRepositoryPort>()
    private val routingContext = mockk<RoutingContext>()
    private val request = mockk<HttpServerRequest>()
    private val socketAddress = mockk<SocketAddress>()

    private lateinit var adapter: RegistrarAuditoriaAdapter

    private data class Snapshot(val id: String, val nome: String)

    @BeforeEach
    fun setup() {
        clearMocks(jwtPort, strategyFactory, repository, routingContext, request, socketAddress)
        adapter = RegistrarAuditoriaAdapter(jwtPort, strategyFactory, repository, routingContext)

        every { routingContext.request() } returns request
        every { request.remoteAddress() } returns socketAddress
        every { socketAddress.host() } returns "192.168.0.1"
    }

    private fun mockStrategy(captor: CapturingSlot<AuditoriaContextoDTO>): AuditoriaStrategy {
        val strategy = mockk<AuditoriaStrategy>()
        every { strategy.executar(capture(captor)) } answers {
            val ctx = captor.captured
            Auditoria.registrar(
                enderecoIp = ctx.enderecoIp,
                referenciaId = ctx.referenciaId,
                eventoAuditoria = EventoAuditoria.criacao(ctx.entidade),
                dados = DadosAuditoria(listOf(CampoAlterado("campo", null, "valor", TipoDadoAuditoria.STRING))),
                responsavelAcao = ctx.responsavelAcao,
            )
        }
        return strategy
    }

    @Test
    fun `deve delegar para strategy correta via factory`() {
        val contextoCaptured = slot<AuditoriaContextoDTO>()
        val strategy = mockStrategy(contextoCaptured)
        val usuarioId = UsuarioId.gerar()

        every { jwtPort.getUsuarioId() } returns usuarioId
        every { strategyFactory.obter(TipoEventoAuditoria.CRIADO) } returns strategy
        every { repository.salvar(any()) } answers { firstArg() }

        adapter.registrar(
            entidade = TipoEntidade.SERVICO,
            evento = TipoEventoAuditoria.CRIADO,
            referenciaId = "ref-123",
            antes = null,
            depois = Snapshot("ref-123", "Novo Serviço"),
        )

        verify(exactly = 1) { strategyFactory.obter(TipoEventoAuditoria.CRIADO) }
        verify(exactly = 1) { strategy.executar(any()) }
        verify(exactly = 1) { repository.salvar(any()) }
    }

    @Test
    fun `deve construir contexto com antes e depois corretos`() {
        val contextoCaptured = slot<AuditoriaContextoDTO>()
        val strategy = mockStrategy(contextoCaptured)
        val antes = Snapshot("ref-1", "Antes")
        val depois = Snapshot("ref-1", "Depois")

        every { jwtPort.getUsuarioId() } returns UsuarioId.gerar()
        every { strategyFactory.obter(TipoEventoAuditoria.ATUALIZADO) } returns strategy
        every { repository.salvar(any()) } answers { firstArg() }

        adapter.registrar(
            entidade = TipoEntidade.VEICULO,
            evento = TipoEventoAuditoria.ATUALIZADO,
            referenciaId = "ref-1",
            antes = antes,
            depois = depois,
        )

        val ctx = contextoCaptured.captured
        assertEquals(TipoEntidade.VEICULO, ctx.entidade)
        assertEquals(TipoEventoAuditoria.ATUALIZADO, ctx.evento)
        assertEquals("ref-1", ctx.referenciaId.value)
        assertEquals(antes, ctx.antes)
        assertEquals(depois, ctx.depois)
    }

    @Test
    fun `deve passar antes null para criacao`() {
        val contextoCaptured = slot<AuditoriaContextoDTO>()
        val strategy = mockStrategy(contextoCaptured)

        every { jwtPort.getUsuarioId() } returns UsuarioId.gerar()
        every { strategyFactory.obter(TipoEventoAuditoria.CRIADO) } returns strategy
        every { repository.salvar(any()) } answers { firstArg() }

        adapter.registrar(
            entidade = TipoEntidade.INSUMO,
            evento = TipoEventoAuditoria.CRIADO,
            referenciaId = "ref-novo",
            antes = null,
            depois = Snapshot("ref-novo", "Novo"),
        )

        assertNull(contextoCaptured.captured.antes)
        assertNotNull(contextoCaptured.captured.depois)
    }

    @Test
    fun `deve capturar IP do request`() {
        val contextoCaptured = slot<AuditoriaContextoDTO>()
        val strategy = mockStrategy(contextoCaptured)

        every { socketAddress.host() } returns "10.0.0.42"
        every { jwtPort.getUsuarioId() } returns UsuarioId.gerar()
        every { strategyFactory.obter(any()) } returns strategy
        every { repository.salvar(any()) } answers { firstArg() }

        adapter.registrar(TipoEntidade.SERVICO, TipoEventoAuditoria.CRIADO, "ref", null, Snapshot("1", "S"))

        assertEquals("10.0.0.42", contextoCaptured.captured.enderecoIp.value)
    }

    @Test
    fun `deve usar IP fallback quando remoteAddress retorna null`() {
        val contextoCaptured = slot<AuditoriaContextoDTO>()
        val strategy = mockStrategy(contextoCaptured)

        every { request.remoteAddress() } returns null
        every { jwtPort.getUsuarioId() } returns UsuarioId.gerar()
        every { strategyFactory.obter(any()) } returns strategy
        every { repository.salvar(any()) } answers { firstArg() }

        adapter.registrar(TipoEntidade.SERVICO, TipoEventoAuditoria.CRIADO, "ref", null, Snapshot("1", "S"))

        assertEquals("127.0.0.1", contextoCaptured.captured.enderecoIp.value)
    }

    @Test
    fun `deve usar referenciaId como responsavel quando JWT falha`() {
        val contextoCaptured = slot<AuditoriaContextoDTO>()
        val strategy = mockStrategy(contextoCaptured)

        every { jwtPort.getUsuarioId() } throws RuntimeException("Token inválido")
        every { strategyFactory.obter(any()) } returns strategy
        every { repository.salvar(any()) } answers { firstArg() }

        adapter.registrar(TipoEntidade.CLIENTE, TipoEventoAuditoria.LOGIN, "user-uuid", null, null)

        assertEquals("user-uuid", contextoCaptured.captured.responsavelAcao.valor)
    }
}
