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

class LogoutAuditoriaStrategyTest {

    private val strategy = LogoutAuditoriaStrategy()

    private fun buildContexto() = AuditoriaContextoDTO(
        entidade = TipoEntidade.MECANICO,
        evento = TipoEventoAuditoria.LOGOUT,
        referenciaId = ReferenciaId("mec-1"),
        antes = null,
        depois = null,
        enderecoIp = EnderecoIp.criar("10.0.0.5"),
        responsavelAcao = UsuarioId.gerar(),
    )

    @Test
    fun `deve suportar apenas evento LOGOUT`() {
        assertTrue(strategy.suporta(TipoEventoAuditoria.LOGOUT))
        assertFalse(strategy.suporta(TipoEventoAuditoria.LOGIN))
        assertFalse(strategy.suporta(TipoEventoAuditoria.REMOVIDO))
    }

    @Test
    fun `deve gerar auditoria com alteracoes nulas para evento`() {
        val auditoria = strategy.executar(buildContexto())

        assertNotNull(auditoria)
        assertNull(auditoria.dados.alteracoes)
    }

    @Test
    fun `deve gerar evento com tipo LOGOUT e entidade MECANICO`() {
        val auditoria = strategy.executar(buildContexto())

        assertEquals(TipoEventoAuditoria.LOGOUT, auditoria.eventoAuditoria.tipo)
        assertEquals(TipoEntidade.MECANICO, auditoria.eventoAuditoria.entidade)
    }
}
