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

class LoginAuditoriaStrategyTest {

    private val strategy = LoginAuditoriaStrategy()

    private fun buildContexto() = AuditoriaContextoDTO(
        entidade = TipoEntidade.CLIENTE,
        evento = TipoEventoAuditoria.LOGIN,
        referenciaId = ReferenciaId("user-1"),
        antes = null,
        depois = null,
        enderecoIp = EnderecoIp.criar("192.168.0.10"),
        responsavelAcao = UsuarioId.gerar(),
    )

    @Test
    fun `deve suportar apenas evento LOGIN`() {
        assertTrue(strategy.suporta(TipoEventoAuditoria.LOGIN))
        assertFalse(strategy.suporta(TipoEventoAuditoria.LOGOUT))
        assertFalse(strategy.suporta(TipoEventoAuditoria.CRIADO))
    }

    @Test
    fun `deve gerar auditoria com alteracoes nulas para evento`() {
        val auditoria = strategy.executar(buildContexto())

        assertNotNull(auditoria)
        assertNull(auditoria.dados.alteracoes)
    }

    @Test
    fun `deve gerar evento com tipo LOGIN e entidade CLIENTE`() {
        val auditoria = strategy.executar(buildContexto())

        assertEquals(TipoEventoAuditoria.LOGIN, auditoria.eventoAuditoria.tipo)
        assertEquals(TipoEntidade.CLIENTE, auditoria.eventoAuditoria.entidade)
    }
}
