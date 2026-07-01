package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class DashboardClienteIT {

    private lateinit var tokenCliente: String
    private lateinit var tokenMecanico: String
    private lateinit var clienteId: String
    private lateinit var mecanicoId: String
    private lateinit var veiculoId: String

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
        val digit = ('0'..'9').toList()
        val alphaNum = alpha + digit
        return "${alpha.random()}${alpha.random()}${alpha.random()}${digit.random()}${alphaNum.random()}${digit.random()}${digit.random()}"
    }

    @BeforeEach
    fun setup() {
        val unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8)
        val emailCliente = "cliente.dash.$unique@email.com"
        val emailMecanico = "mecanico.dash.$unique@email.com"

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Dashboard IT",
                  "email": "$emailCliente",
                  "senha": "#Tiee123456",
                  "telefone": "11988887777",
                  "cpf": "${gerarCpf()}",
                  "dataNascimento": "1990-05-10"
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

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Dashboard IT",
                  "email": "$emailMecanico",
                  "senha": "#Tiee123456",
                  "telefone": "11977776666",
                  "cpf": "${gerarCpf()}",
                  "dataNascimento": "1985-03-20",
                  "nivel": "PLENO",
                  "valorHora": 80.00
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
    }

    @Test
    fun `deve retornar dashboard completo do cliente`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
            .get("/dashboard/clientes/$clienteId")
            .then()
            .statusCode(200)
            .body("usuarioId", equalTo(clienteId))
            .body("usuarioNome", notNullValue())
            .body("resumo", notNullValue())
            .body("resumo.veiculosCadastrados", greaterThanOrEqualTo(1))
            .body("resumo.totalOrdens", greaterThanOrEqualTo(0))
            .body("ordensAtivas", notNullValue())
            .body("ordensRecentes", notNullValue())
            .body("veiculos", notNullValue())
            .body("veiculos", hasSize<Any>(greaterThanOrEqualTo(1)))
            .body("dataAtualizacao", notNullValue())
    }

    @Test
    fun `deve retornar dashboard com veiculos incluindo dados de estatisticas`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
            .get("/dashboard/clientes/$clienteId")
            .then()
            .statusCode(200)
            .body("veiculos[0].id", notNullValue())
            .body("veiculos[0].placa", notNullValue())
            .body("veiculos[0].marca", equalTo("Honda"))
            .body("veiculos[0].modelo", equalTo("Civic"))
            .body("veiculos[0].totalOrdens", greaterThanOrEqualTo(0))
            .body("veiculos[0].dataCriacao", notNullValue())
    }

    @Test
    fun `deve retornar dashboard com ordens ativas apos criar uma OS`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "motivo": "Revisão semestral",
                  "clienteId": "$clienteId",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$veiculoId"
                }
                """.trimIndent()
            )
            .post("/ordem-servico")
            .then()
            .statusCode(201)

        given()
            .header("Authorization", "Bearer $tokenCliente")
            .get("/dashboard/clientes/$clienteId")
            .then()
            .statusCode(200)
            .body("resumo.ordensAtivas", greaterThanOrEqualTo(1))
            .body("resumo.totalOrdens", greaterThanOrEqualTo(1))
            .body("ordensAtivas", hasSize<Any>(greaterThanOrEqualTo(1)))
            .body("ordensAtivas[0].motivo", equalTo("Revisão semestral"))
            .body("ordensAtivas[0].status", equalTo("RECEBIDA"))
            .body("ordensAtivas[0].diasEmAndamento", greaterThanOrEqualTo(0))
    }

    @Test
    fun `deve retornar 403 quando cliente tenta acessar dashboard de outro cliente`() {
        val outroClienteId = UUID.randomUUID().toString()

        given()
            .header("Authorization", "Bearer $tokenCliente")
            .get("/dashboard/clientes/$outroClienteId")
            .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 403 quando mecanico tenta acessar dashboard de cliente`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .get("/dashboard/clientes/$clienteId")
            .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 401 ao acessar dashboard sem token`() {
        given()
            .get("/dashboard/clientes/$clienteId")
            .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar resumo com contadores corretos para cliente sem ordens`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
            .get("/dashboard/clientes/$clienteId")
            .then()
            .statusCode(200)
            .body("resumo.ordensAtivas", equalTo(0))
            .body("resumo.ordensConcluidas", equalTo(0))
            .body("resumo.ordensCanceladas", equalTo(0))
            .body("resumo.totalOrdens", equalTo(0))
    }

    @Test
    fun `deve retornar veiculos com total_gasto zerado quando nao ha orcamentos aprovados`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
            .get("/dashboard/clientes/$clienteId")
            .then()
            .statusCode(200)
            .body("veiculos[0].totalGasto", equalTo(0))
    }
}
