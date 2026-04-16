package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class ClienteIT {

    private lateinit var tokenCliente: String
    private lateinit var clienteId: String
    private lateinit var tokenMecanico: String

    @BeforeEach
    fun setup() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Busca IT",
                  "email": "cliente.busca.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900006666",
                  "cpf": "48963996093",
                  "dataNascimento": "1994-07-30"
                }
                """.trimIndent()
            )
            .post("/clientes")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Cliente IT",
                  "email": "mecanico.cliente.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900007777",
                  "cpf": "26328821093",
                  "dataNascimento": "1979-02-12",
                  "nivel": "JUNIOR",
                  "valorHora": 60.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        val loginCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.busca.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        tokenCliente = loginCliente.getString("token")
        clienteId    = loginCliente.getString("usuarioId")

        tokenMecanico = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "mecanico.cliente.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token")
    }

    @Test
    fun `deve buscar cliente por id como o proprio cliente e retornar 200`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/clientes/$clienteId")
        .then()
            .statusCode(200)
            .body("id", equalTo(clienteId))
            .body("nome", notNullValue())
            .body("email", equalTo("cliente.busca.it@email.com"))
            .body("ativo", equalTo(true))
    }

    @Test
    fun `deve buscar cliente por id como mecanico e retornar 200`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/clientes/$clienteId")
        .then()
            .statusCode(200)
            .body("id", equalTo(clienteId))
            .body("email", equalTo("cliente.busca.it@email.com"))
    }

    @Test
    fun `deve retornar 404 ao buscar cliente inexistente`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/clientes/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 403 quando cliente tenta buscar dados de outro usuario`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/clientes/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao buscar cliente`() {
        given()
        .`when`()
            .get("/clientes/$clienteId")
        .then()
            .statusCode(401)
    }
}
