package br.com.servicetrack.domain.auditoria.vo

import br.com.servicetrack.domain.auditoria.CampoAlterado
import br.com.servicetrack.domain.auditoria.enums.TipoDadoAuditoria
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DadosAuditoriaTest {

    private enum class Status { ATIVO, INATIVO }

    private data class Snapshot(
        val id: String,
        val nome: String,
        val ativo: Boolean,
        val quantidade: Int,
    )

    private data class SnapshotTipos(
        val valor: BigDecimal,
        val data: LocalDateTime,
        val status: Status,
        val tags: List<String>,
    )

    private data class SnapshotNumericos(
        val longVal: Long,
        val doubleVal: Double,
    )

    private data class SnapshotMapa(
        val mapa: Map<String, Any>,
    )

    private data class SnapshotNullavel(
        val campo1: String?,
        val campo2: String?,
    )

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
        assertEquals(2, dados.alteracoes.size)
    }

    // --- Factory: criacao ---

    @Test
    fun `criacao deve gerar campos com valorAntes nulo e valorDepois preenchido`() {
        val snapshot = Snapshot("1", "Produto", true, 10)
        val dados = DadosAuditoria.criacao(snapshot)

        assertTrue(dados.temAlteracoes())
        dados.alteracoes.forEach { campo ->
            assertNull(campo.valorAntes)
            assertNotNull(campo.valorDepois)
        }
    }

    @Test
    fun `criacao deve incluir todos os campos do objeto`() {
        val snapshot = Snapshot("1", "Produto", true, 10)
        val dados = DadosAuditoria.criacao(snapshot)

        val nomes = dados.alteracoes.map { it.campo }
        assertTrue(nomes.contains("nome"))
        assertTrue(nomes.contains("ativo"))
        assertTrue(nomes.contains("quantidade"))
    }

    @Test
    fun `criacao deve resolver o tipo STRING para campos String`() {
        val snapshot = Snapshot("1", "Produto", true, 5)
        val dados = DadosAuditoria.criacao(snapshot)

        val campoNome = dados.alteracoes.first { it.campo == "nome" }
        assertEquals(TipoDadoAuditoria.STRING, campoNome.tipo)
    }

    @Test
    fun `criacao deve resolver o tipo BOOLEAN para campos Boolean`() {
        val snapshot = Snapshot("1", "Produto", true, 5)
        val dados = DadosAuditoria.criacao(snapshot)

        val campoAtivo = dados.alteracoes.first { it.campo == "ativo" }
        assertEquals(TipoDadoAuditoria.BOOLEAN, campoAtivo.tipo)
    }

    @Test
    fun `criacao deve resolver o tipo INTEGER para campos Int`() {
        val snapshot = Snapshot("1", "Produto", true, 5)
        val dados = DadosAuditoria.criacao(snapshot)

        val campoQtd = dados.alteracoes.first { it.campo == "quantidade" }
        assertEquals(TipoDadoAuditoria.INTEGER, campoQtd.tipo)
    }

    // --- Factory: remocao ---

    @Test
    fun `remocao deve gerar campos com valorDepois nulo e valorAntes preenchido`() {
        val snapshot = Snapshot("1", "Produto", true, 10)
        val dados = DadosAuditoria.remocao(snapshot)

        assertTrue(dados.temAlteracoes())
        dados.alteracoes.forEach { campo ->
            assertNotNull(campo.valorAntes)
            assertNull(campo.valorDepois)
        }
    }

    @Test
    fun `remocao deve incluir todos os campos do objeto`() {
        val snapshot = Snapshot("1", "Produto", false, 3)
        val dados = DadosAuditoria.remocao(snapshot)

        val nomes = dados.alteracoes.map { it.campo }
        assertTrue(nomes.contains("nome"))
        assertTrue(nomes.contains("ativo"))
    }

    // --- Factory: atualizacao ---

    @Test
    fun `atualizacao deve retornar apenas os campos que sofreram alteração`() {
        val antes = Snapshot("1", "Produto", true, 10)
        val depois = Snapshot("1", "Produto Atualizado", true, 10)
        val dados = DadosAuditoria.atualizacao(antes, depois)

        assertTrue(dados.temAlteracoes())
        assertEquals(1, dados.alteracoes.size)
        assertEquals("nome", dados.alteracoes.first().campo)
    }

    @Test
    fun `atualizacao deve conter valorAntes e valorDepois corretos`() {
        val antes = Snapshot("1", "Original", true, 5)
        val depois = Snapshot("1", "Atualizado", true, 5)
        val dados = DadosAuditoria.atualizacao(antes, depois)

        val campo = dados.alteracoes.first { it.campo == "nome" }
        assertEquals("Original", campo.valorAntes)
        assertEquals("Atualizado", campo.valorDepois)
    }

    @Test
    fun `atualizacao deve retornar lista vazia quando nenhum campo mudou`() {
        val snapshot = Snapshot("1", "Produto", true, 10)
        val dados = DadosAuditoria.atualizacao(snapshot, snapshot)

        assertFalse(dados.temAlteracoes())
    }

    @Test
    fun `atualizacao deve capturar múltiplos campos alterados`() {
        val antes = Snapshot("1", "Original", true, 5)
        val depois = Snapshot("1", "Atualizado", false, 20)
        val dados = DadosAuditoria.atualizacao(antes, depois)

        assertEquals(3, dados.alteracoes.size)
    }

    // --- resolverTipo: cobertura de branches ---

    @Test
    fun `criacao deve resolver tipo DECIMAL para campos BigDecimal`() {
        val snapshot = SnapshotTipos(BigDecimal("9.99"), LocalDateTime.now(), Status.ATIVO, listOf("a"))
        val dados = DadosAuditoria.criacao(snapshot)

        val campo = dados.alteracoes.first { it.campo == "valor" }
        assertEquals(TipoDadoAuditoria.DECIMAL, campo.tipo)
    }

    @Test
    fun `criacao deve resolver tipo DATA para campos LocalDateTime`() {
        val snapshot = SnapshotTipos(BigDecimal("1.0"), LocalDateTime.now(), Status.ATIVO, listOf("a"))
        val dados = DadosAuditoria.criacao(snapshot)

        val campo = dados.alteracoes.first { it.campo == "data" }
        assertEquals(TipoDadoAuditoria.DATA, campo.tipo)
    }

    @Test
    fun `criacao deve resolver tipo ENUM para campos enum`() {
        val snapshot = SnapshotTipos(BigDecimal("1.0"), LocalDateTime.now(), Status.ATIVO, listOf("a"))
        val dados = DadosAuditoria.criacao(snapshot)

        val campo = dados.alteracoes.first { it.campo == "status" }
        assertEquals(TipoDadoAuditoria.ENUM, campo.tipo)
    }

    @Test
    fun `criacao deve resolver tipo ARRAY para campos List`() {
        val snapshot = SnapshotTipos(BigDecimal("1.0"), LocalDateTime.now(), Status.ATIVO, listOf("tag1", "tag2"))
        val dados = DadosAuditoria.criacao(snapshot)

        val campo = dados.alteracoes.first { it.campo == "tags" }
        assertEquals(TipoDadoAuditoria.ARRAY, campo.tipo)
    }

    @Test
    fun `criacao deve resolver tipo STRING para campo nulo`() {
        val campo = CampoAlterado<String>("campo", null, null, TipoDadoAuditoria.STRING)
        val dados = DadosAuditoria(listOf(campo))

        assertEquals(TipoDadoAuditoria.STRING, dados.alteracoes.first().tipo)
    }

    @Test
    fun `criacao deve resolver tipo INTEGER para campo Long`() {
        val snapshot = SnapshotNumericos(longVal = 100L, doubleVal = 1.5)
        val dados = DadosAuditoria.criacao(snapshot)

        val campo = dados.alteracoes.first { it.campo == "longVal" }
        assertEquals(TipoDadoAuditoria.INTEGER, campo.tipo)
    }

    @Test
    fun `criacao deve resolver tipo DECIMAL para campo Double`() {
        val snapshot = SnapshotNumericos(longVal = 1L, doubleVal = 3.14)
        val dados = DadosAuditoria.criacao(snapshot)

        val campo = dados.alteracoes.first { it.campo == "doubleVal" }
        assertEquals(TipoDadoAuditoria.DECIMAL, campo.tipo)
    }

    @Test
    fun `criacao deve resolver tipo OBJETO para campos Map`() {
        val snapshot = SnapshotMapa(mapa = mapOf("chave" to "valor"))
        val dados = DadosAuditoria.criacao(snapshot)

        val campo = dados.alteracoes.first { it.campo == "mapa" }
        assertEquals(TipoDadoAuditoria.OBJETO, campo.tipo)
    }

    @Test
    fun `atualizacao deve incluir campo nulo quando valor muda de nulo para preenchido`() {
        val antes = SnapshotNullavel(campo1 = null, campo2 = "fixo")
        val depois = SnapshotNullavel(campo1 = "novo", campo2 = "fixo")
        val dados = DadosAuditoria.atualizacao(antes, depois)

        assertEquals(1, dados.alteracoes.size)
        val campo = dados.alteracoes.first()
        assertNull(campo.valorAntes)
        assertEquals("novo", campo.valorDepois)
    }
}
