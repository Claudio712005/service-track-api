package br.com.servicetrack.infrastructure.auditoria.strategy

import br.com.servicetrack.application.auditoria.dto.AuditoriaContextoDTO
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.auditoria.vo.EnderecoIp
import br.com.servicetrack.domain.auditoria.vo.ReferenciaId
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CriacaoAuditoriaStrategyTest {

    private val strategy = CriacaoAuditoriaStrategy()

    private data class Snapshot(val id: String, val nome: String, val ativo: Boolean)

    private fun buildContexto(depois: Any) = AuditoriaContextoDTO(
        entidade = TipoEntidade.SERVICO,
        evento = TipoEventoAuditoria.CRIADO,
        referenciaId = ReferenciaId("ref-1"),
        antes = null,
        depois = depois,
        enderecoIp = EnderecoIp.criar("127.0.0.1"),
        responsavelAcao = UsuarioId.gerar(),
    )

    @Test
    fun `deve suportar apenas evento CRIADO`() {
        assertTrue(strategy.suporta(TipoEventoAuditoria.CRIADO))
        assertFalse(strategy.suporta(TipoEventoAuditoria.ATUALIZADO))
        assertFalse(strategy.suporta(TipoEventoAuditoria.REMOVIDO))
        assertFalse(strategy.suporta(TipoEventoAuditoria.LOGIN))
    }

    @Test
    fun `deve gerar auditoria com valorAntes nulo e valorDepois preenchido para todos os campos`() {
        val snapshot = Snapshot("1", "Troca de óleo", true)
        val ctx = buildContexto(snapshot)

        val auditoria = strategy.executar(ctx)

        assertNotNull(auditoria)
        auditoria.dados.alteracoes?.forEach { campo ->
            assertNull(campo.valorAntes, "Campo '${campo.campo}' deveria ter valorAntes nulo na criação")
            assertNotNull(campo.valorDepois, "Campo '${campo.campo}' deveria ter valorDepois preenchido na criação")
        }
    }

    @Test
    fun `deve incluir todos os campos do objeto na criacao`() {
        val snapshot = Snapshot("1", "Troca de óleo", true)
        val ctx = buildContexto(snapshot)

        val auditoria = strategy.executar(ctx)

        val nomes = auditoria.dados.alteracoes?.map { it.campo }
        assertTrue(nomes!!.contains("id"))
        assertTrue(nomes.contains("nome"))
        assertTrue(nomes.contains("ativo"))
    }

    @Test
    fun `deve gerar evento com tipo CRIADO e entidade correta`() {
        val ctx = buildContexto(Snapshot("1", "Serviço", true))

        val auditoria = strategy.executar(ctx)

        assertEquals(TipoEventoAuditoria.CRIADO, auditoria.eventoAuditoria.tipo)
        assertEquals(TipoEntidade.SERVICO, auditoria.eventoAuditoria.entidade)
    }

    @Test
    fun `deve preservar referenciaId e enderecoIp do contexto`() {
        val ctx = buildContexto(Snapshot("1", "Serviço", true))

        val auditoria = strategy.executar(ctx)

        assertEquals("ref-1", auditoria.referenciaId.value)
        assertEquals("127.0.0.1", auditoria.enderecoIp.value)
    }
}
