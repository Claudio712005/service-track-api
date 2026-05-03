package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class MecanicoIT {

    private lateinit var tokenMecanico: String
    private lateinit var mecanicoId: String
    private lateinit var tokenCliente: String

    @BeforeEach
    fun setup() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Busca IT",
                  "email": "mecanico.busca.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900002222",
                  "cpf": "85157577028",
                  "dataNascimento": "1982-04-18",
                  "nivel": "SENIOR",
                  "valorHora": 110.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Mecanico IT",
                  "email": "cliente.mecanico.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900003333",
                  "cpf": "38568559042",
                  "dataNascimento": "1995-08-22"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val loginMecanico = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "mecanico.busca.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        tokenMecanico = loginMecanico.getString("token")
        mecanicoId   = loginMecanico.getString("usuarioId")

        tokenCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.mecanico.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token")
    }

    @Test
    fun `deve buscar mecanico por id e retornar 200`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/mecanicos/$mecanicoId")
        .then()
            .statusCode(200)
            .body("usuarioId", equalTo(mecanicoId))
            .body("nome", notNullValue())
            .body("email", equalTo("mecanico.busca.it@email.com"))
            .body("nivel", equalTo("SENIOR"))
    }

    @Test
    fun `deve retornar 404 ao buscar mecanico inexistente`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/mecanicos/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve listar mecanicos e retornar 200`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/mecanicos")
        .then()
            .statusCode(200)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao buscar mecanico`() {
        given()
        .`when`()
            .get("/mecanicos/$mecanicoId")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao listar mecanicos`() {
        given()
        .`when`()
            .get("/mecanicos")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 403 quando cliente tenta buscar mecanico`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/mecanicos/$mecanicoId")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 403 quando cliente tenta listar mecanicos`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/mecanicos")
        .then()
            .statusCode(403)
    }
}
