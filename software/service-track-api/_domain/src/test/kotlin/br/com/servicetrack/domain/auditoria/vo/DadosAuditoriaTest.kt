package br.com.servicetrack.domain.auditoria.vo

import br.com.servicetrack.domain.auditoria.CampoAlterado
import br.com.servicetrack.domain.auditoria.enums.TipoDadoAuditoria
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DadosAuditoriaTest {

    @Test
    fun `deve retornar true em temAlteracoes quando há campos alterados`() {
        val campo = CampoAlterado("nome", "antes", "depois", TipoDadoAuditoria.STRING)
        val dados = DadosAuditoria(listOf(campo))
        assertTrue(dados.temAlteracoes())
    }

    @Test
    fun `deve retornar false em temAlteracoes quando lista de alterações está vazia`() {
        val dados = DadosAuditoria(emptyList())
        assertFalse(dados.temAlteracoes())
    }

    @Test
    fun `deve armazenar múltiplos campos alterados`() {
        val campos = listOf(
            CampoAlterado("nome", "João", "Maria", TipoDadoAuditoria.STRING),
            CampoAlterado("idade", 30, 31, TipoDadoAuditoria.INTEGER),
        )
        val dados = DadosAuditoria(campos)
        assertTrue(dados.temAlteracoes())
        assert(dados.alteracoes.size == 2)
    }
}
