package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class OrdemServicoFluxoIT {

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
        val emailMecanico = "mecanico.fluxo.$unique@email.com"
        val emailCliente = "cliente.fluxo.$unique@email.com"

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Fluxo IT",
                  "email": "$emailMecanico",
                  "senha": "#Tiee123456",
                  "telefone": "11944445555",
                  "cpf": "${gerarCpf()}",
                  "dataNascimento": "1982-04-15",
                  "nivel": "SENIOR",
                  "valorHora": 120.00
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
                  "nome": "Cliente Fluxo IT",
                  "email": "$emailCliente",
                  "senha": "#Tiee123456",
                  "telefone": "11955556666",
                  "cpf": "${gerarCpf()}",
                  "dataNascimento": "1995-07-20"
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
                  "modelo": "Civic",
                  "marca": "Honda",
                  "ano": 2021,
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
                  "nomeServico": "Troca de Óleo",
                  "descricaoServico": "Substituição do óleo do motor e filtro",
                  "valorReferencia": 150.00
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
                  "nome": "Filtro de Óleo",
                  "descricao": "Filtro para motor 1.0",
                  "custo": 25.50,
                  "qtdEstoqueInicial": 20,
                  "estoqueMinimo": 2
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

    private fun criarOs(): String = given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer $tokenMecanico")
        .body(
            """
            {
              "motivo": "Revisão geral do veículo",
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

    @Test
    fun `deve executar fluxo completo de aprovacao e finalizacao`() {
        val osId = criarOs()

        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .post("/ordem-servico/$osId/diagnostico")
            .then()
            .statusCode(200)
            .body("status", equalTo("EM_DIAGNOSTICO"))

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "servicos": [{"servicoId": "$servicoId", "valorCobrado": 150.00}],
                  "insumos": [{"insumoId": "$insumoId", "quantidade": 2}]
                }
                """.trimIndent()
            )
            .put("/ordem-servico/$osId/diagnostico/itens")
            .then()
            .statusCode(200)
            .body("itensServico", notNullValue())

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body("""{"prazoEntrega": "2026-12-31"}""")
            .post("/ordem-servico/$osId/orcamento")
            .then()
            .statusCode(200)
            .body("status", equalTo("AGUARDANDO_APROVACAO"))

        given()
            .header("Authorization", "Bearer $tokenCliente")
            .post("/ordem-servico/$osId/orcamento/aprovacao")
            .then()
            .statusCode(200)
            .body("status", equalTo("EM_EXECUCAO"))

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
            .body("""{"observacao": "Serviço realizado com sucesso"}""")
            .patch("/ordem-servico/$osId/itens/$itemId/concluir")
            .then()
            .statusCode(200)

        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .post("/ordem-servico/$osId/finalizacao")
            .then()
            .statusCode(200)
            .body("status", equalTo("FINALIZADA"))

        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .post("/ordem-servico/$osId/entrega")
            .then()
            .statusCode(200)
            .body("status", equalTo("ENTREGUE"))
    }

    @Test
    fun `deve executar fluxo de reprovacao de orcamento`() {
        val osId = criarOs()

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
                  "servicos": [{"servicoId": "$servicoId", "valorCobrado": 150.00}],
                  "insumos": [{"insumoId": "$insumoId", "quantidade": 1}]
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
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body("""{"motivo": "Valores muito altos"}""")
            .post("/ordem-servico/$osId/orcamento/reprovacao")
            .then()
            .statusCode(200)
            .body("status", equalTo("CANCELADA"))
    }

    @Test
    fun `deve cancelar OS no status RECEBIDA`() {
        val osId = criarOs()

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body("""{"motivo": "Desistência do serviço"}""")
            .post("/ordem-servico/$osId/cancelamento")
            .then()
            .statusCode(200)
            .body("status", equalTo("CANCELADA"))
    }

    @Test
    fun `deve retornar 404 ao enviar para diagnostico OS inexistente`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .post("/ordem-servico/00000000-0000-0000-0000-000000000000/diagnostico")
            .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 403 quando mecanico tenta aprovar orcamento`() {
        val osId = criarOs()

        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .post("/ordem-servico/$osId/orcamento/aprovacao")
            .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 403 quando cliente tenta enviar OS para diagnostico`() {
        val osId = criarOs()

        given()
            .header("Authorization", "Bearer $tokenCliente")
            .post("/ordem-servico/$osId/diagnostico")
            .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 401 ao acessar endpoint sem token`() {
        given()
            .post("/ordem-servico/00000000-0000-0000-0000-000000000000/diagnostico")
            .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 400 quando observacao de conclusao esta vazia`() {
        val osId = criarOs()

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
                  "servicos": [{"servicoId": "$servicoId", "valorCobrado": 150.00}],
                  "insumos": [{"insumoId": "$insumoId", "quantidade": 1}]
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
            .body("""{"observacao": ""}""")
            .patch("/ordem-servico/$osId/itens/$itemId/concluir")
            .then()
            .statusCode(400)
    }
}
