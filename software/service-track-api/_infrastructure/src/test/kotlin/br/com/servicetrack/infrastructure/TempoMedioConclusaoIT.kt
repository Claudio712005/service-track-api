package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class TempoMedioConclusaoIT {

    private lateinit var tokenMecanico: String
    private lateinit var tokenCliente: String
    private lateinit var mecanicoId: String
    private lateinit var clienteId: String
    private lateinit var veiculoId: String
    private lateinit var servicoId: String
    private lateinit var insumoId: String

    private fun gerarCpf(): String {
        val digits = (1..9).map { (0..9).random() }
        var sum = digits.mapIndexed { i, d -> d * (10 - i) }.sum()
        val d10 = (sum % 11).let { if (it < 2) 0 else 11 - it }
        sum = digits.mapIndexed { i, d -> d * (11 - i) }.sum() + d10 * 2
        val d11 = (sum % 11).let { if (it < 2) 0 else 11 - it }
        return "${digits.joinToString("")}$d10$d11"
    }

    private fun gerarPlaca(): String {
        val alpha = ('A'..'Z').toList()
        val alphaNum = alpha + ('0'..'9').toList()
        val digit = ('0'..'9').toList()
        return "${alpha.random()}${alpha.random()}${alpha.random()}${digit.random()}${alphaNum.random()}${digit.random()}${digit.random()}"
    }

    @BeforeEach
    fun setup() {
        val unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8)
        val emailMecanico = "mecanico.tempo.$unique@email.com"
        val emailCliente = "cliente.tempo.$unique@email.com"

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Tempo IT",
                  "email": "$emailMecanico",
                  "senha": "#Tiee123456",
                  "telefone": "11911112222",
                  "cpf": "${gerarCpf()}",
                  "dataNascimento": "1980-06-10",
                  "nivel": "SENIOR",
                  "valorHora": 100.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")
            .then()
            .statusCode(201)

        val loginMecanico = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "$emailMecanico", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        tokenMecanico = loginMecanico.getString("token")
        mecanicoId = loginMecanico.getString("usuarioId")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Tempo IT",
                  "email": "$emailCliente",
                  "senha": "#Tiee123456",
                  "telefone": "11933334444",
                  "cpf": "${gerarCpf()}",
                  "dataNascimento": "1992-11-25"
                }
                """.trimIndent()
            )
            .post("/clientes")
            .then()
            .statusCode(201)

        val loginCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "$emailCliente", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        tokenCliente = loginCliente.getString("token")
        clienteId = loginCliente.getString("usuarioId")

        veiculoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "placa": "${gerarPlaca()}",
                  "modelo": "Corolla",
                  "marca": "Toyota",
                  "ano": 2022,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        servicoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nomeServico": "Troca de Velas",
                  "descricaoServico": "Substituição das velas de ignição",
                  "valorReferencia": 200.00
                }
                """.trimIndent()
            )
            .post("/servicos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        insumoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nome": "Vela NGK",
                  "descricao": "Vela de ignição NGK",
                  "custo": 30.00,
                  "qtdEstoqueInicial": 20,
                  "estoqueMinimo": 4
                }
                """.trimIndent()
            )
            .post("/insumos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")
    }

    private fun executarFluxoCompletoAteConclusion(): String {
        val osId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "motivo": "Substituição das velas",
                  "clienteId": "$clienteId",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$veiculoId"
                }
                """.trimIndent()
            )
            .post("/ordem-servico")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .post("/ordem-servico/$osId/diagnostico")
            .then()
            .statusCode(200)

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "servicos": [{"servicoId": "$servicoId", "valorCobrado": 200.00}],
                  "insumos": [{"insumoId": "$insumoId", "quantidade": 4}]
                }
                """.trimIndent()
            )
            .put("/ordem-servico/$osId/diagnostico/itens")
            .then()
            .statusCode(200)

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body("""{"prazoEntrega": "2026-12-31"}""")
            .post("/ordem-servico/$osId/orcamento")
            .then()
            .statusCode(200)

        given()
            .header("Authorization", "Bearer $tokenCliente")
            .post("/ordem-servico/$osId/orcamento/aprovacao")
            .then()
            .statusCode(200)

        val itemId = given()
            .header("Authorization", "Bearer $tokenMecanico")
            .get("/ordem-servico/$osId")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("itensServico[0].id")

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body("""{"observacao": "Velas substituídas com sucesso"}""")
            .patch("/ordem-servico/$osId/itens/$itemId/concluir")
            .then()
            .statusCode(200)

        return osId
    }

    @Test
    fun `deve retornar tempo medio em horas com itens concluidos`() {
        executarFluxoCompletoAteConclusion()

        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .queryParam("unidade", "HORAS")
        .`when`()
            .get("/servicos/$servicoId/tempo-medio-conclusao")
        .then()
            .statusCode(200)
            .body("servicoId", equalTo(servicoId))
            .body("unidade", equalTo("HORAS"))
            .body("totalAmostras", equalTo(1))
            .body("tempoMedio", notNullValue())
    }

    @Test
    fun `deve retornar zero amostras para servico sem itens concluidos`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .queryParam("unidade", "MINUTOS")
        .`when`()
            .get("/servicos/$servicoId/tempo-medio-conclusao")
        .then()
            .statusCode(200)
            .body("servicoId", equalTo(servicoId))
            .body("totalAmostras", equalTo(0))
            .body("tempoMedio", equalTo(0.0f))
    }

    @Test
    fun `deve retornar 404 para servico inexistente`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .queryParam("unidade", "HORAS")
        .`when`()
            .get("/servicos/00000000-0000-0000-0000-000000000000/tempo-medio-conclusao")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 401 sem token de autenticacao`() {
        given()
            .queryParam("unidade", "HORAS")
        .`when`()
            .get("/servicos/$servicoId/tempo-medio-conclusao")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar tempo medio em segundos minutos e dias consistentemente`() {
        executarFluxoCompletoAteConclusion()

        val segundos = given()
            .header("Authorization", "Bearer $tokenMecanico")
            .queryParam("unidade", "SEGUNDOS")
            .get("/servicos/$servicoId/tempo-medio-conclusao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getDouble("tempoMedio")

        val minutos = given()
            .header("Authorization", "Bearer $tokenMecanico")
            .queryParam("unidade", "MINUTOS")
            .get("/servicos/$servicoId/tempo-medio-conclusao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getDouble("tempoMedio")

        val horas = given()
            .header("Authorization", "Bearer $tokenMecanico")
            .queryParam("unidade", "HORAS")
            .get("/servicos/$servicoId/tempo-medio-conclusao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getDouble("tempoMedio")

        val dias = given()
            .header("Authorization", "Bearer $tokenMecanico")
            .queryParam("unidade", "DIAS")
            .get("/servicos/$servicoId/tempo-medio-conclusao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getDouble("tempoMedio")

        // All units should be consistent conversions of the same duration
        assertEquals(segundos / 60.0, minutos, 0.5)
        assertEquals(minutos / 60.0, horas, 0.01)
        assertEquals(horas / 24.0, dias, 0.001)
    }

    private fun assertEquals(expected: Double, actual: Double, delta: Double) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual, delta)
    }
}
