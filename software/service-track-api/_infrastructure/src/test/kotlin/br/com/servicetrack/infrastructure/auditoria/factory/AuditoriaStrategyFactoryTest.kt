package br.com.servicetrack.infrastructure.auditoria.factory

import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.infrastructure.auditoria.strategy.AtualizacaoAuditoriaStrategy
import br.com.servicetrack.infrastructure.auditoria.strategy.CriacaoAuditoriaStrategy
import br.com.servicetrack.infrastructure.auditoria.strategy.LoginAuditoriaStrategy
import br.com.servicetrack.infrastructure.auditoria.strategy.LogoutAuditoriaStrategy
import br.com.servicetrack.infrastructure.auditoria.strategy.RemocaoAuditoriaStrategy
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AuditoriaStrategyFactoryTest {

    private val factory = AuditoriaStrategyFactory(
        listOf(
            CriacaoAuditoriaStrategy(),
            AtualizacaoAuditoriaStrategy(),
            RemocaoAuditoriaStrategy(),
            LoginAuditoriaStrategy(),
            LogoutAuditoriaStrategy(),
        ),
    )

    @Test
    fun `deve retornar CriacaoAuditoriaStrategy para evento CRIADO`() {
        val strategy = factory.obter(TipoEventoAuditoria.CRIADO)
        assertTrue(strategy is CriacaoAuditoriaStrategy)
    }

    @Test
    fun `deve retornar AtualizacaoAuditoriaStrategy para evento ATUALIZADO`() {
        val strategy = factory.obter(TipoEventoAuditoria.ATUALIZADO)
        assertTrue(strategy is AtualizacaoAuditoriaStrategy)
    }

    @Test
    fun `deve retornar RemocaoAuditoriaStrategy para evento REMOVIDO`() {
        val strategy = factory.obter(TipoEventoAuditoria.REMOVIDO)
        assertTrue(strategy is RemocaoAuditoriaStrategy)
    }

    @Test
    fun `deve retornar LoginAuditoriaStrategy para evento LOGIN`() {
        val strategy = factory.obter(TipoEventoAuditoria.LOGIN)
        assertTrue(strategy is LoginAuditoriaStrategy)
    }

    @Test
    fun `deve retornar LogoutAuditoriaStrategy para evento LOGOUT`() {
        val strategy = factory.obter(TipoEventoAuditoria.LOGOUT)
        assertTrue(strategy is LogoutAuditoriaStrategy)
    }

    @Test
    fun `deve lancar excecao para evento sem strategy registrada`() {
        val factoryVazia = AuditoriaStrategyFactory(emptyList())

        assertThrows<IllegalArgumentException> {
            factoryVazia.obter(TipoEventoAuditoria.CRIADO)
        }
    }

    @Test
    fun `deve lancar excecao para evento ATIVADO sem strategy`() {
        assertThrows<IllegalArgumentException> {
            factory.obter(TipoEventoAuditoria.ATIVADO)
        }
    }
}
