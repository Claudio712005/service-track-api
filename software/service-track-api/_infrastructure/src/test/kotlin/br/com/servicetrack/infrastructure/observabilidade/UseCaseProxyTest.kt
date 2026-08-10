package br.com.servicetrack.infrastructure.observabilidade

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.observabilidade.annotation.Observavel
import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import br.com.servicetrack.application.observabilidade.enums.CodigoUseCase
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.TracerProvider
import org.jboss.logging.MDC
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UseCaseProxyTest {

    data class Req(@field:Rastreavel val id: String, val senha: String)
    data class Res(@field:Rastreavel val ordemServicoId: String)

    interface CriarOsUseCase {
        fun criarOrdemServico(req: Req): Res
    }

    interface SemAnotacaoUseCase {
        fun executar(req: Req): Res
    }

    class CriarOsService(private val falha: Throwable? = null) : CriarOsUseCase {
        @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.CRIADO)
        @Observavel(codigo = CodigoUseCase.OS_CRIAR)
        override fun criarOrdemServico(req: Req): Res {
            falha?.let { throw it }
            return Res("os-1")
        }
    }

    class SemAnotacaoService : SemAnotacaoUseCase {
        override fun executar(req: Req) = Res("os-2")
    }

    private class AuditoriaEspia : RegistrarAuditoriaPort {
        var chamadas = 0
        var ultimaEntidade: TipoEntidade? = null

        override fun registrar(
            entidade: TipoEntidade,
            evento: TipoEventoAuditoria,
            referenciaId: String,
            antes: Any?,
            depois: Any?,
        ) {
            chamadas++
            ultimaEntidade = entidade
        }
    }

    private lateinit var registry: MeterRegistry
    private lateinit var tracer: Tracer
    private lateinit var proxy: UseCaseProxy
    private lateinit var auditoria: AuditoriaEspia

    @BeforeEach
    fun preparar() {
        registry = SimpleMeterRegistry()
        tracer = TracerProvider.noop().get("teste")
        proxy = UseCaseProxy(registry, tracer)
        auditoria = AuditoriaEspia()
    }

    private fun timer(resultado: String) =
        registry.find("servicetrack.usecase.duracao")
            .tag("use_case", CodigoUseCase.OS_CRIAR.name)
            .tag("entidade", TipoEntidade.ORDEM_SERVICO.name)
            .tag("resultado", resultado)
            .timer()

    @Test
    fun `delega a chamada e devolve o resultado`() {
        val alvo = proxy.envolver(CriarOsService(), CriarOsUseCase::class.java, auditoria)

        assertEquals("os-1", alvo.criarOrdemServico(Req("c-1", "segredo")).ordemServicoId)
    }

    @Test
    fun `registra timer e contador no sucesso`() {
        val alvo = proxy.envolver(CriarOsService(), CriarOsUseCase::class.java, auditoria)

        alvo.criarOrdemServico(Req("c-1", "segredo"))

        assertEquals(1, timer("sucesso")?.count())
        assertEquals(
            1.0,
            registry.counter(
                "servicetrack.usecase.execucoes",
                "use_case", CodigoUseCase.OS_CRIAR.name,
                "entidade", TipoEntidade.ORDEM_SERVICO.name,
                "resultado", "sucesso",
            ).count(),
        )
    }

    @Test
    fun `registra timer de erro e propaga a excecao`() {
        val alvo = proxy.envolver(
            CriarOsService(OperacaoNegadaException("criar", "cliente inativo")),
            CriarOsUseCase::class.java,
            auditoria,
        )

        assertThrows(OperacaoNegadaException::class.java) { alvo.criarOrdemServico(Req("c-1", "x")) }

        assertEquals(1, timer("erro")?.count())
        assertNull(timer("sucesso"))
    }

    @Test
    fun `nao regride a auditoria quando o metodo tem as duas anotacoes`() {
        val alvo = proxy.envolver(CriarOsService(), CriarOsUseCase::class.java, auditoria)

        alvo.criarOrdemServico(Req("c-1", "segredo"))

        assertEquals(1, auditoria.chamadas)
        assertEquals(TipoEntidade.ORDEM_SERVICO, auditoria.ultimaEntidade)
    }

    @Test
    fun `nao audita quando a chamada falha`() {
        val alvo = proxy.envolver(
            CriarOsService(IllegalStateException("boom")),
            CriarOsUseCase::class.java,
            auditoria,
        )

        assertThrows(IllegalStateException::class.java) { alvo.criarOrdemServico(Req("c-1", "x")) }

        assertEquals(0, auditoria.chamadas)
    }

    @Test
    fun `metodo sem Observavel continua funcionando sem metrica`() {
        val alvo = proxy.envolver(SemAnotacaoService(), SemAnotacaoUseCase::class.java, auditoria)

        assertEquals("os-2", alvo.executar(Req("c-1", "x")).ordemServicoId)
        assertNull(registry.find("servicetrack.usecase.duracao").timer())
    }

    @Test
    fun `limpa o contexto de log apos o sucesso`() {
        val alvo = proxy.envolver(CriarOsService(), CriarOsUseCase::class.java, auditoria)

        alvo.criarOrdemServico(Req("c-1", "segredo"))

        listOf("use_case", "entidade", "duracao_ms", "id", "ordemServicoId").forEach {
            assertNull(MDC.get(it)) { "MDC vazou a chave $it" }
        }
    }

    @Test
    fun `limpa o contexto de log apos a falha`() {
        val alvo = proxy.envolver(
            CriarOsService(IllegalStateException("boom")),
            CriarOsUseCase::class.java,
            auditoria,
        )

        assertThrows(IllegalStateException::class.java) { alvo.criarOrdemServico(Req("c-1", "x")) }

        listOf("use_case", "entidade", "erro_codigo", "erro_tipo", "erro_mensagem").forEach {
            assertNull(MDC.get(it)) { "MDC vazou a chave $it" }
        }
    }

    @Test
    fun `usa o antesProvider para compor o contexto de auditoria`() {
        val alvo = proxy.envolver(
            CriarOsService(),
            CriarOsUseCase::class.java,
            auditoria,
            antesProvider = { args -> (args.first() as Req).id },
        )

        assertNotNull(alvo.criarOrdemServico(Req("c-9", "x")))
        assertEquals(1, auditoria.chamadas)
    }
}
