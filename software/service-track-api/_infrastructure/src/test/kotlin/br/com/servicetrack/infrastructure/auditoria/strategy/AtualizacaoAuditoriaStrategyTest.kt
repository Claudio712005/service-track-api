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

class AtualizacaoAuditoriaStrategyTest {

    private val strategy = AtualizacaoAuditoriaStrategy()

    private data class Snapshot(val id: String, val nome: String, val preco: Double, val ativo: Boolean)

    private fun buildContexto(antes: Any?, depois: Any) = AuditoriaContextoDTO(
        entidade = TipoEntidade.VEICULO,
        evento = TipoEventoAuditoria.ATUALIZADO,
        referenciaId = ReferenciaId("ref-upd"),
        antes = antes,
        depois = depois,
        enderecoIp = EnderecoIp.criar("192.168.1.1"),
        responsavelAcao = UsuarioId.gerar(),
    )

    @Test
    fun `deve suportar apenas evento ATUALIZADO`() {
        assertTrue(strategy.suporta(TipoEventoAuditoria.ATUALIZADO))
        assertFalse(strategy.suporta(TipoEventoAuditoria.CRIADO))
        assertFalse(strategy.suporta(TipoEventoAuditoria.REMOVIDO))
    }

    @Test
    fun `deve retornar apenas campos alterados com valorAntes e valorDepois preenchidos`() {
        val antes = Snapshot("1", "Original", 100.0, true)
        val depois = Snapshot("1", "Atualizado", 150.0, true)
        val ctx = buildContexto(antes, depois)

        val auditoria = strategy.executar(ctx)

        assertEquals(2, auditoria.dados.alteracoes?.size)
        auditoria.dados.alteracoes?.forEach { campo ->
            assertNotNull(campo.valorAntes, "Campo '${campo.campo}' deveria ter valorAntes preenchido na atualização")
            assertNotNull(campo.valorDepois, "Campo '${campo.campo}' deveria ter valorDepois preenchido na atualização")
        }
    }

    @Test
    fun `deve conter valores corretos de antes e depois nos campos alterados`() {
        val antes = Snapshot("1", "Original", 100.0, true)
        val depois = Snapshot("1", "Novo Nome", 100.0, true)
        val ctx = buildContexto(antes, depois)

        val auditoria = strategy.executar(ctx)

        val campo = auditoria.dados.alteracoes?.first { it.campo == "nome" }
        assertEquals("Original", campo?.valorAntes)
        assertEquals("Novo Nome", campo?.valorDepois)
    }

    @Test
    fun `nao deve incluir campos que nao mudaram`() {
        val antes = Snapshot("1", "Nome", 100.0, true)
        val depois = Snapshot("1", "Nome", 200.0, true)
        val ctx = buildContexto(antes, depois)

        val auditoria = strategy.executar(ctx)

        val nomes = auditoria.dados.alteracoes?.map { it.campo }
        assertEquals(1, nomes?.size)
        assertTrue(nomes!!.contains("preco"))
        assertFalse(nomes.contains("id"))
        assertFalse(nomes.contains("nome"))
        assertFalse(nomes.contains("ativo"))
    }

    @Test
    fun `deve gerar atualizacaoSemAntes quando antes eh nulo`() {
        val depois = Snapshot("1", "Sem Antes", 50.0, false)
        val ctx = buildContexto(antes = null, depois = depois)

        val auditoria = strategy.executar(ctx)

        auditoria.dados.alteracoes?.forEach { campo ->
            assertNull(campo.valorAntes, "Campo '${campo.campo}' deveria ter valorAntes nulo quando antes ausente")
            assertNotNull(campo.valorDepois, "Campo '${campo.campo}' deveria ter valorDepois preenchido")
        }
        assertEquals(4, auditoria.dados.alteracoes?.size)
    }

    @Test
    fun `deve gerar evento com tipo ATUALIZADO e entidade correta`() {
        val antes = Snapshot("1", "A", 1.0, true)
        val depois = Snapshot("1", "B", 1.0, true)
        val ctx = buildContexto(antes, depois)

        val auditoria = strategy.executar(ctx)

        assertEquals(TipoEventoAuditoria.ATUALIZADO, auditoria.eventoAuditoria.tipo)
        assertEquals(TipoEntidade.VEICULO, auditoria.eventoAuditoria.entidade)
    }
}
