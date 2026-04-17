package br.com.servicetrack.domain.auditoria

import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class EventoAuditoriaTest {

    @Test
    fun `deve criar evento de criacao com tipo e descrição corretos`() {
        val evento = EventoAuditoria.criacao(TipoEntidade.CLIENTE)

        assertEquals(TipoEntidade.CLIENTE, evento.entidade)
        assertEquals(TipoEventoAuditoria.CRIADO, evento.tipo)
        assertEquals("${TipoEntidade.CLIENTE.descricao} criado(a)", evento.descricao)
    }

    @Test
    fun `deve criar evento de atualizado com tipo e descrição corretos`() {
        val evento = EventoAuditoria.atualizado(TipoEntidade.VEICULO)

        assertEquals(TipoEntidade.VEICULO, evento.entidade)
        assertEquals(TipoEventoAuditoria.ATUALIZADO, evento.tipo)
        assertEquals("${TipoEntidade.VEICULO.descricao} atualizado(a)", evento.descricao)
    }

    @Test
    fun `deve criar evento de removido com tipo e descrição corretos`() {
        val evento = EventoAuditoria.removido(TipoEntidade.INSUMO)

        assertEquals(TipoEntidade.INSUMO, evento.entidade)
        assertEquals(TipoEventoAuditoria.REMOVIDO, evento.tipo)
        assertEquals("${TipoEntidade.INSUMO.descricao} removido(a)", evento.descricao)
    }

    @Test
    fun `deve criar evento de desativado com tipo e descrição corretos`() {
        val evento = EventoAuditoria.desativado(TipoEntidade.MECANICO)

        assertEquals(TipoEntidade.MECANICO, evento.entidade)
        assertEquals(TipoEventoAuditoria.DESATIVADO, evento.tipo)
        assertEquals("${TipoEntidade.MECANICO.descricao} desativado(a)", evento.descricao)
    }

    @Test
    fun `deve criar evento de ativado com tipo e descrição corretos`() {
        val evento = EventoAuditoria.ativado(TipoEntidade.SERVICO)

        assertEquals(TipoEntidade.SERVICO, evento.entidade)
        assertEquals(TipoEventoAuditoria.ATIVADO, evento.tipo)
        assertEquals("${TipoEntidade.SERVICO.descricao} ativado(a)", evento.descricao)
    }

    @Test
    fun `deve criar evento de login para CLIENTE`() {
        val evento = EventoAuditoria.login(TipoEntidade.CLIENTE)

        assertEquals(TipoEntidade.CLIENTE, evento.entidade)
        assertEquals(TipoEventoAuditoria.LOGIN, evento.tipo)
        assertEquals("${TipoEntidade.CLIENTE.descricao} realizou login", evento.descricao)
    }

    @Test
    fun `deve criar evento de login para MECANICO`() {
        val evento = EventoAuditoria.login(TipoEntidade.MECANICO)

        assertEquals(TipoEntidade.MECANICO, evento.entidade)
        assertEquals(TipoEventoAuditoria.LOGIN, evento.tipo)
        assertEquals("${TipoEntidade.MECANICO.descricao} realizou login", evento.descricao)
    }

    @Test
    fun `deve lançar exceção ao criar evento de login para entidade não permitida`() {
        val exception = assertThrows<DomainException> {
            EventoAuditoria.login(TipoEntidade.VEICULO)
        }
        assertEquals("Evento de login só pode ser criado para Cliente ou Mecânico", exception.message)
    }

    @Test
    fun `deve criar evento de logout para CLIENTE`() {
        val evento = EventoAuditoria.logout(TipoEntidade.CLIENTE)

        assertEquals(TipoEntidade.CLIENTE, evento.entidade)
        assertEquals(TipoEventoAuditoria.LOGOUT, evento.tipo)
        assertEquals("${TipoEntidade.CLIENTE.descricao} realizou logout", evento.descricao)
    }

    @Test
    fun `deve criar evento de logout para MECANICO`() {
        val evento = EventoAuditoria.logout(TipoEntidade.MECANICO)

        assertEquals(TipoEntidade.MECANICO, evento.entidade)
        assertEquals(TipoEventoAuditoria.LOGOUT, evento.tipo)
        assertEquals("${TipoEntidade.MECANICO.descricao} realizou logout", evento.descricao)
    }

    @Test
    fun `deve lançar exceção ao criar evento de logout para entidade não permitida`() {
        val exception = assertThrows<DomainException> {
            EventoAuditoria.logout(TipoEntidade.ORDEM_SERVICO)
        }
        assertEquals("Evento de login só pode ser criado para Cliente ou Mecânico", exception.message)
    }

    @Test
    fun `deve criar evento de mudanca sensivel com tipo e descrição corretos`() {
        val evento = EventoAuditoria.mudancaSensivel(TipoEntidade.CLIENTE)

        assertEquals(TipoEntidade.CLIENTE, evento.entidade)
        assertEquals(TipoEventoAuditoria.ALTERACAO_SENSIVEL, evento.tipo)
        assertEquals("${TipoEntidade.CLIENTE.descricao} sofreu mudança sensível", evento.descricao)
    }

    @Test
    fun `deve lançar exceção ao criar login para ORCAMENTO`() {
        assertThrows<DomainException> {
            EventoAuditoria.login(TipoEntidade.ORCAMENTO)
        }
    }

    @Test
    fun `deve lançar exceção ao criar logout para INSUMO`() {
        assertThrows<DomainException> {
            EventoAuditoria.logout(TipoEntidade.INSUMO)
        }
    }
}
