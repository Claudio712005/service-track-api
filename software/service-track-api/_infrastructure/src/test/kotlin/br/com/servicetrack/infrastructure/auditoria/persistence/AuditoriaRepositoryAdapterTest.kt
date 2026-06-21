package br.com.servicetrack.infrastructure.auditoria.persistence

import br.com.servicetrack.domain.auditoria.CampoAlterado
import br.com.servicetrack.domain.auditoria.enums.TipoDadoAuditoria
import br.com.servicetrack.domain.auditoria.vo.DadosAuditoria
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AuditoriaRepositoryAdapterTest {

    private val objectMapper = ObjectMapper()

    private data class Snapshot(val id: String, val nome: String, val preco: Double, val ativo: Boolean)

    private fun serializarDados(dados: DadosAuditoria): String {
        val lista = dados.alteracoes?.map { campo ->
            mapOf(
                "campo" to campo.campo,
                "valorAntes" to campo.valorAntes?.toString(),
                "valorDepois" to campo.valorDepois?.toString(),
                "tipo" to campo.tipo.name,
            )
        }
        return objectMapper.writeValueAsString(lista)
    }

    private fun parseJson(json: String): List<Map<String, String?>> =
        objectMapper.readValue(json)

    @Test
    fun `criacao deve gerar JSON com valorAntes null e valorDepois preenchido para todos os campos`() {
        val snapshot = Snapshot("abc-123", "Troca de óleo", 89.90, true)
        val dados = DadosAuditoria.criacao(snapshot)

        val json = serializarDados(dados)
        val campos = parseJson(json)

        assertEquals(4, campos.size)
        campos.forEach { campo ->
            assertNull(campo["valorAntes"], "Campo '${campo["campo"]}' deveria ter valorAntes null na criação, mas era: ${campo["valorAntes"]}")
            assertNotNull(campo["valorDepois"], "Campo '${campo["campo"]}' deveria ter valorDepois preenchido na criação")
        }
    }

    @Test
    fun `criacao deve conter valores corretos no JSON`() {
        val snapshot = Snapshot("abc-123", "Freio", 150.0, true)
        val dados = DadosAuditoria.criacao(snapshot)

        val json = serializarDados(dados)
        val campos = parseJson(json)

        val campoNome = campos.first { it["campo"] == "nome" }
        assertNull(campoNome["valorAntes"])
        assertEquals("Freio", campoNome["valorDepois"])
        assertEquals("STRING", campoNome["tipo"])

        val campoPreco = campos.first { it["campo"] == "preco" }
        assertNull(campoPreco["valorAntes"])
        assertEquals("150.0", campoPreco["valorDepois"])
        assertEquals("DECIMAL", campoPreco["tipo"])

        val campoAtivo = campos.first { it["campo"] == "ativo" }
        assertNull(campoAtivo["valorAntes"])
        assertEquals("true", campoAtivo["valorDepois"])
        assertEquals("BOOLEAN", campoAtivo["tipo"])
    }

    @Test
    fun `remocao deve gerar JSON com valorAntes preenchido e valorDepois null para todos os campos`() {
        val snapshot = Snapshot("xyz-789", "Parafuso M6", 2.50, true)
        val dados = DadosAuditoria.remocao(snapshot)

        val json = serializarDados(dados)
        val campos = parseJson(json)

        assertEquals(4, campos.size)
        campos.forEach { campo ->
            assertNotNull(campo["valorAntes"], "Campo '${campo["campo"]}' deveria ter valorAntes preenchido na remoção")
            assertNull(campo["valorDepois"], "Campo '${campo["campo"]}' deveria ter valorDepois null na remoção, mas era: ${campo["valorDepois"]}")
        }
    }

    @Test
    fun `remocao deve conter valores corretos no JSON`() {
        val snapshot = Snapshot("xyz-789", "Parafuso M6", 2.50, false)
        val dados = DadosAuditoria.remocao(snapshot)

        val json = serializarDados(dados)
        val campos = parseJson(json)

        val campoNome = campos.first { it["campo"] == "nome" }
        assertEquals("Parafuso M6", campoNome["valorAntes"])
        assertNull(campoNome["valorDepois"])

        val campoId = campos.first { it["campo"] == "id" }
        assertEquals("xyz-789", campoId["valorAntes"])
        assertNull(campoId["valorDepois"])

        val campoAtivo = campos.first { it["campo"] == "ativo" }
        assertEquals("false", campoAtivo["valorAntes"])
        assertNull(campoAtivo["valorDepois"])
    }

    @Test
    fun `remocaoSemEstado deve gerar JSON com campo estado ATIVO para REMOVIDO`() {
        val dados = DadosAuditoria.remocaoSemEstado()

        val json = serializarDados(dados)
        val campos = parseJson(json)

        assertEquals(1, campos.size)
        val campo = campos.first()
        assertEquals("estado", campo["campo"])
        assertEquals("ATIVO", campo["valorAntes"])
        assertEquals("REMOVIDO", campo["valorDepois"])
        assertEquals("STRING", campo["tipo"])
    }

    @Test
    fun `atualizacao deve gerar JSON apenas com campos alterados e ambos valores preenchidos`() {
        val antes = Snapshot("1", "Original", 100.0, true)
        val depois = Snapshot("1", "Atualizado", 200.0, true)
        val dados = DadosAuditoria.atualizacao(antes, depois)

        val json = serializarDados(dados)
        val campos = parseJson(json)

        assertEquals(2, campos.size)
        campos.forEach { campo ->
            assertNotNull(campo["valorAntes"], "Campo '${campo["campo"]}' deveria ter valorAntes preenchido na atualização")
            assertNotNull(campo["valorDepois"], "Campo '${campo["campo"]}' deveria ter valorDepois preenchido na atualização")
        }
    }

    @Test
    fun `atualizacao deve conter diff correto no JSON`() {
        val antes = Snapshot("1", "Nome A", 50.0, true)
        val depois = Snapshot("1", "Nome B", 75.0, true)
        val dados = DadosAuditoria.atualizacao(antes, depois)

        val json = serializarDados(dados)
        val campos = parseJson(json)

        val campoNome = campos.first { it["campo"] == "nome" }
        assertEquals("Nome A", campoNome["valorAntes"])
        assertEquals("Nome B", campoNome["valorDepois"])

        val campoPreco = campos.first { it["campo"] == "preco" }
        assertEquals("50.0", campoPreco["valorAntes"])
        assertEquals("75.0", campoPreco["valorDepois"])

        val nomesAlterados = campos.map { it["campo"] }
        assertNull(nomesAlterados.find { it == "id" }, "Campo 'id' não deveria aparecer no diff (não mudou)")
        assertNull(nomesAlterados.find { it == "ativo" }, "Campo 'ativo' não deveria aparecer no diff (não mudou)")
    }

    @Test
    fun `atualizacao sem nenhuma mudanca deve gerar JSON lista vazia`() {
        val snapshot = Snapshot("1", "Mesmo", 10.0, true)
        val dados = DadosAuditoria.atualizacao(snapshot, snapshot)

        val json = serializarDados(dados)
        val campos = parseJson(json)

        assertEquals(0, campos.size)
    }

    @Test
    fun `atualizacaoSemAntes deve gerar JSON com valorAntes null e valorDepois preenchido`() {
        val depois = Snapshot("1", "Depois", 30.0, false)
        val dados = DadosAuditoria.atualizacaoSemAntes(depois)

        val json = serializarDados(dados)
        val campos = parseJson(json)

        assertEquals(4, campos.size)
        campos.forEach { campo ->
            assertNull(campo["valorAntes"], "Campo '${campo["campo"]}' deveria ter valorAntes null em atualizacaoSemAntes")
            assertNotNull(campo["valorDepois"], "Campo '${campo["campo"]}' deveria ter valorDepois preenchido")
        }
    }

    @Test
    fun `evento deve gerar JSON null para login e logout`() {
        val dados = DadosAuditoria.evento()

        val json = serializarDados(dados)

        assertEquals("null", json)
    }

    @Test
    fun `JSON deve conter tipo correto para cada campo`() {
        val snapshot = Snapshot("1", "Item", 9.99, true)
        val dados = DadosAuditoria.criacao(snapshot)

        val json = serializarDados(dados)
        val campos = parseJson(json)

        assertEquals("STRING", campos.first { it["campo"] == "id" }["tipo"])
        assertEquals("STRING", campos.first { it["campo"] == "nome" }["tipo"])
        assertEquals("DECIMAL", campos.first { it["campo"] == "preco" }["tipo"])
        assertEquals("BOOLEAN", campos.first { it["campo"] == "ativo" }["tipo"])
    }

    @Test
    fun `JSON de criacao deve ser valido e parseable`() {
        val snapshot = Snapshot("id-1", "Teste", 1.0, true)
        val dados = DadosAuditoria.criacao(snapshot)

        val json = serializarDados(dados)

        val parsed: List<Map<String, Any?>> = objectMapper.readValue(json)
        assertEquals(4, parsed.size)
        parsed.forEach { entry ->
            assert(entry.containsKey("campo"))
            assert(entry.containsKey("valorAntes"))
            assert(entry.containsKey("valorDepois"))
            assert(entry.containsKey("tipo"))
        }
    }

    @Test
    fun `campo com valor null na atualizacao deve serializar como null no JSON`() {
        val campo = CampoAlterado<String>("observacao", null, "nova obs", TipoDadoAuditoria.STRING)
        val dados = DadosAuditoria(listOf(campo))

        val json = serializarDados(dados)
        val campos = parseJson(json)

        val obs = campos.first()
        assertNull(obs["valorAntes"])
        assertEquals("nova obs", obs["valorDepois"])
    }
}
