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

class RemocaoAuditoriaStrategyTest {

    private val strategy = RemocaoAuditoriaStrategy()

    private data class Snapshot(val id: String, val nome: String, val valor: Double)

    private fun buildContexto(antes: Any? = null) = AuditoriaContextoDTO(
        entidade = TipoEntidade.INSUMO,
        evento = TipoEventoAuditoria.REMOVIDO,
        referenciaId = ReferenciaId("ref-rem"),
        antes = antes,
        depois = null,
        enderecoIp = EnderecoIp.criar("10.0.0.1"),
        responsavelAcao = UsuarioId.gerar(),
    )

    @Test
    fun `deve suportar apenas evento REMOVIDO`() {
        assertTrue(strategy.suporta(TipoEventoAuditoria.REMOVIDO))
        assertFalse(strategy.suporta(TipoEventoAuditoria.CRIADO))
        assertFalse(strategy.suporta(TipoEventoAuditoria.ATUALIZADO))
    }

    @Test
    fun `deve gerar auditoria com valorAntes preenchido e valorDepois nulo quando antes fornecido`() {
        val snapshot = Snapshot("1", "Parafuso", 2.50)
        val ctx = buildContexto(antes = snapshot)

        val auditoria = strategy.executar(ctx)

        assertNotNull(auditoria)
        auditoria.dados.alteracoes?.forEach { campo ->
            assertNotNull(campo.valorAntes, "Campo '${campo.campo}' deveria ter valorAntes preenchido na remoção")
            assertNull(campo.valorDepois, "Campo '${campo.campo}' deveria ter valorDepois nulo na remoção")
        }
    }

    @Test
    fun `deve incluir todos os campos do objeto na remocao`() {
        val snapshot = Snapshot("1", "Parafuso", 2.50)
        val ctx = buildContexto(antes = snapshot)

        val auditoria = strategy.executar(ctx)

        val nomes = auditoria.dados.alteracoes?.map { it.campo }
        assertTrue(nomes!!.contains("id"))
        assertTrue(nomes.contains("nome"))
        assertTrue(nomes.contains("valor"))
    }

    @Test
    fun `deve gerar fallback remocaoSemEstado quando antes eh nulo`() {
        val ctx = buildContexto(antes = null)

        val auditoria = strategy.executar(ctx)

        assertEquals(1, auditoria.dados.alteracoes?.size)
        val campo = auditoria.dados.alteracoes?.first()
        assertEquals("estado", campo?.campo)
        assertEquals("ATIVO", campo?.valorAntes)
        assertEquals("REMOVIDO", campo?.valorDepois)
    }

    @Test
    fun `deve gerar evento com tipo REMOVIDO e entidade correta`() {
        val ctx = buildContexto(antes = Snapshot("1", "Item", 1.0))

        val auditoria = strategy.executar(ctx)

        assertEquals(TipoEventoAuditoria.REMOVIDO, auditoria.eventoAuditoria.tipo)
        assertEquals(TipoEntidade.INSUMO, auditoria.eventoAuditoria.entidade)
    }
}
