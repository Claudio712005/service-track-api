package br.com.servicetrack.infrastructure

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.infrastructure.auditoria.AuditoriaProxy
import io.mockk.clearMocks
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AuditoriaProxyTest {

    data class RespostaDTO(val id: String, val valor: String)

    interface ServicoTeste {
        fun executar(id: String): RespostaDTO
        fun executarSemAuditoria(id: String): RespostaDTO
        fun executarComFalha(id: String): RespostaDTO
    }

    inner class ServicoTesteImpl : ServicoTeste {
        @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.CRIADO)
        override fun executar(id: String): RespostaDTO = RespostaDTO(id, "ok")

        override fun executarSemAuditoria(id: String): RespostaDTO = RespostaDTO(id, "sem auditoria")

        @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.CRIADO)
        override fun executarComFalha(id: String): RespostaDTO = throw RuntimeException("Erro simulado")
    }

    private val auditoriaPort = mockk<RegistrarAuditoriaPort>()
    private lateinit var proxy: ServicoTeste

    @BeforeEach
    fun setup() {
        clearMocks(auditoriaPort)
        proxy = AuditoriaProxy.envolver(ServicoTesteImpl(), ServicoTeste::class.java, auditoriaPort)
    }

    @Test
    fun `deve registrar auditoria apos execucao bem sucedida`() {
        val entidadeSlot = slot<TipoEntidade>()
        val eventoSlot = slot<TipoEventoAuditoria>()
        val referenciaSlot = slot<String>()
        justRun {
            auditoriaPort.registrar(
                capture(entidadeSlot),
                capture(eventoSlot),
                capture(referenciaSlot),
                any(),
                any(),
            )
        }
        val idEsperado = "abc-123"

        proxy.executar(idEsperado)

        verify(exactly = 1) { auditoriaPort.registrar(any(), any(), any(), any(), any()) }
        assertEquals(TipoEntidade.ORDEM_SERVICO, entidadeSlot.captured)
        assertEquals(TipoEventoAuditoria.CRIADO, eventoSlot.captured)
        assertEquals(idEsperado, referenciaSlot.captured)
    }

    @Test
    fun `deve retornar resultado correto apos execucao`() {
        justRun { auditoriaPort.registrar(any(), any(), any(), any(), any()) }

        val resultado = proxy.executar("meu-id")

        assertEquals("meu-id", resultado.id)
        assertEquals("ok", resultado.valor)
    }

    @Test
    fun `nao deve registrar auditoria em metodo sem anotacao Auditavel`() {
        val resultado = proxy.executarSemAuditoria("id-sem-auditoria")

        verify(exactly = 0) { auditoriaPort.registrar(any(), any(), any(), any(), any()) }
        assertEquals("sem auditoria", resultado.valor)
    }

    @Test
    fun `deve limpar contexto e propagar excecao quando metodo falha`() {
        AuditoriaContextoHolder.registrarAntes("estado-anterior")

        assertThrows<RuntimeException> {
            proxy.executarComFalha("id-falha")
        }

        verify(exactly = 0) { auditoriaPort.registrar(any(), any(), any(), any(), any()) }
        assertNull(AuditoriaContextoHolder.obterAntes())
    }

    @Test
    fun `deve usar estado anterior registrado no contexto como valor de antes`() {
        val estadoAnterior = RespostaDTO("antes-id", "estado anterior")
        val antesSlot = slot<Any>()
        justRun {
            auditoriaPort.registrar(any(), any(), any(), capture(antesSlot), any())
        }
        AuditoriaContextoHolder.registrarAntes(estadoAnterior)

        proxy.executar("novo-id")

        assertEquals(estadoAnterior, antesSlot.captured)
    }
}
