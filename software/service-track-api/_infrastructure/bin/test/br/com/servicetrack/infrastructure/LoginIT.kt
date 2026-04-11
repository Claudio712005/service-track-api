package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class LoginIT {

    @BeforeEach
    fun setup() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Login Cliente",
                  "email": "login.cliente@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11955554444",
                  "cpf": "98765432100",
                  "dataNascimento": "1990-01-15"
                }
                """.trimIndent()
            )
            .post("/clientes")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Login Mecânico",
                  "email": "login.mecanico@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11944443333",
                  "cpf": "52998224725",
                  "dataNascimento": "1985-03-20",
                  "nivel": "PLENO",
                  "valorHora": 75.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")
    }

    @Test
    fun `deve realizar login de cliente com sucesso e retornar token`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "email": "login.cliente@email.com",
                  "senha": "#Tiee123456"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/autenticacao")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("email", equalTo("login.cliente@email.com"))
            .body("usuarioId", notNullValue())
    }

    @Test
    fun `deve realizar login de mecanico com sucesso e retornar token`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "email": "login.mecanico@email.com",
                  "senha": "#Tiee123456"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/autenticacao")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("email", equalTo("login.mecanico@email.com"))
    }

    @Test
    fun `deve retornar 401 quando email nao existir`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"email": "naoexiste@email.com", "senha": "#Tiee123456"}""")
        .`when`()
            .post("/autenticacao")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 401 quando senha estiver incorreta`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"email": "login.cliente@email.com", "senha": "#SenhaErrada1"}""")
        .`when`()
            .post("/autenticacao")
        .then()
            .statusCode(401)
    }
}
