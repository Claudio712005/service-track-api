package br.com.servicetrack.domain.auditoria

import br.com.servicetrack.domain.auditoria.enums.TipoDadoAuditoria
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CampoAlteradoTest {

    @Test
    fun `deve criar CampoAlterado com todos os valores preenchidos`() {
        val campo = CampoAlterado("nome", "João", "Maria", TipoDadoAuditoria.STRING)

        assertEquals("nome", campo.campo)
        assertEquals("João", campo.valorAntes)
        assertEquals("Maria", campo.valorDepois)
        assertEquals(TipoDadoAuditoria.STRING, campo.tipo)
    }

    @Test
    fun `deve criar CampoAlterado com valorAntes nulo`() {
        val campo = CampoAlterado<String>("email", null, "novo@email.com", TipoDadoAuditoria.STRING)

        assertNull(campo.valorAntes)
        assertEquals("novo@email.com", campo.valorDepois)
    }

    @Test
    fun `deve criar CampoAlterado com valorDepois nulo`() {
        val campo = CampoAlterado<String>("email", "antigo@email.com", null, TipoDadoAuditoria.STRING)

        assertEquals("antigo@email.com", campo.valorAntes)
        assertNull(campo.valorDepois)
    }

    @Test
    fun `deve criar CampoAlterado com ambos valores nulos`() {
        val campo = CampoAlterado<String>("observacao", null, null, TipoDadoAuditoria.STRING)

        assertNull(campo.valorAntes)
        assertNull(campo.valorDepois)
    }

    @Test
    fun `deve criar CampoAlterado com tipo inteiro`() {
        val campo = CampoAlterado("quantidade", 5, 10, TipoDadoAuditoria.INTEGER)

        assertEquals(5, campo.valorAntes)
        assertEquals(10, campo.valorDepois)
        assertEquals(TipoDadoAuditoria.INTEGER, campo.tipo)
    }

    @Test
    fun `deve criar CampoAlterado com tipo booleano`() {
        val campo = CampoAlterado("ativo", true, false, TipoDadoAuditoria.BOOLEAN)

        assertEquals(true, campo.valorAntes)
        assertEquals(false, campo.valorDepois)
        assertEquals(TipoDadoAuditoria.BOOLEAN, campo.tipo)
    }
}
