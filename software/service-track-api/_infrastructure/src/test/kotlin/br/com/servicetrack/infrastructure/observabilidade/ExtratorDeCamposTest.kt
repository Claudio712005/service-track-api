package br.com.servicetrack.infrastructure.observabilidade

import br.com.servicetrack.application.observabilidade.annotation.Mascarado
import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExtratorDeCamposTest {

    data class Requisicao(
        @field:Rastreavel val id: String,
        @field:Mascarado(visiveis = 2) val cpf: String,
        val senha: String,
        val email: String,
    )

    data class Resposta(
        @field:Rastreavel val ordemServicoId: String,
        @field:Rastreavel(nome = "situacao") val status: String,
    )

    class Envelope(val valor: String)

    data class ComEnvelope(@field:Rastreavel val usuarioId: Envelope)

    interface Alvo {
        fun executar(req: Requisicao): Resposta
        fun porId(@Rastreavel(nome = "cliente_id") id: String): Resposta
        fun mascarandoParametro(@Mascarado(nome = "documento", visiveis = 3) cpf: String): Resposta
    }

    private val metodoExecutar = Alvo::class.java.getMethod("executar", Requisicao::class.java)
    private val metodoPorId = Alvo::class.java.getMethod("porId", String::class.java)
    private val metodoMascarado = Alvo::class.java.getMethod("mascarandoParametro", String::class.java)

    private val requisicao = Requisicao(
        id = "abc-123",
        cpf = "52998224725",
        senha = "SenhaSuperSecreta",
        email = "cliente@example.com",
    )

    @Test
    fun `expoe apenas campos marcados`() {
        val campos = ExtratorDeCampos.extrair(metodoExecutar, arrayOf(requisicao), null)

        assertEquals("abc-123", campos["id"])
        assertTrue(campos.containsKey("cpf"))
        assertFalse(campos.containsKey("senha"))
        assertFalse(campos.containsKey("email"))
    }

    @Test
    fun `nao vaza senha em nenhum valor`() {
        val campos = ExtratorDeCampos.extrair(metodoExecutar, arrayOf(requisicao), null)

        assertFalse(campos.values.any { it.contains("SenhaSuperSecreta") })
    }

    @Test
    fun `mascara revelando apenas os ultimos digitos`() {
        val campos = ExtratorDeCampos.extrair(metodoExecutar, arrayOf(requisicao), null)

        assertEquals("***25", campos["cpf"])
    }

    @Test
    fun `mascara nao vaza o comprimento do valor`() {
        assertEquals(ExtratorDeCampos.mascarar("1234567890", 2).length, ExtratorDeCampos.mascarar("12345", 2).length)
    }

    @Test
    fun `oculta por completo quando o valor e menor que a janela visivel`() {
        assertEquals("***", ExtratorDeCampos.mascarar("12", 2))
        assertEquals("***", ExtratorDeCampos.mascarar("qualquer", 0))
    }

    @Test
    fun `usa o nome informado na anotacao`() {
        val campos = ExtratorDeCampos.extrair(metodoExecutar, emptyArray(), Resposta("os-9", "RECEBIDA"))

        assertEquals("os-9", campos["ordemServicoId"])
        assertEquals("RECEBIDA", campos["situacao"])
        assertFalse(campos.containsKey("status"))
    }

    @Test
    fun `le anotacao de parametro`() {
        val campos = ExtratorDeCampos.extrair(metodoPorId, arrayOf("u-77"), null)

        assertEquals("u-77", campos["cliente_id"])
    }

    @Test
    fun `desembrulha objeto que expoe valor`() {
        val campos = ExtratorDeCampos.extrair(metodoExecutar, arrayOf(ComEnvelope(Envelope("u-88"))), null)

        assertEquals("u-88", campos["usuarioId"])
    }

    @Test
    fun `mascara parametro anotado`() {
        val campos = ExtratorDeCampos.extrair(metodoMascarado, arrayOf("52998224725"), null)

        assertEquals("***725", campos["documento"])
    }

    @Test
    fun `ignora argumento nulo e tipos da biblioteca padrao`() {
        val campos = ExtratorDeCampos.extrair(metodoExecutar, arrayOf(null, "texto solto", 42), null)

        assertTrue(campos.isEmpty())
    }
}
