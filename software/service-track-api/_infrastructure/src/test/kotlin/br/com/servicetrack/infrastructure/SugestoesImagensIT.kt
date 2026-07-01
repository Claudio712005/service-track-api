package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class SugestoesImagensIT {

    private lateinit var tokenCliente: String

    @BeforeEach
    fun configurar() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Imagens IT",
                  "email": "cliente.imagens.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11977778888",
                  "cpf": "55566677720",
                  "dataNascimento": "1990-03-15"
                }
                """.trimIndent()
            )
            .post("/clientes")

        tokenCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.imagens.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token")
    }

    @Test
    fun `deve retornar sugestoes de imagens para marca e modelo informados`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
            .queryParam("marca", "Toyota")
            .queryParam("modelo", "Corolla")
        .`when`()
            .get("/veiculos/imagens/sugestoes")
        .then()
            .statusCode(200)
            .body("imagens", notNullValue())
            .body("imagens", hasSize<String>(3))
    }

    @Test
    fun `deve retornar 401 quando sem autenticacao`() {
        given()
            .queryParam("marca", "Toyota")
            .queryParam("modelo", "Corolla")
        .`when`()
            .get("/veiculos/imagens/sugestoes")
        .then()
            .statusCode(401)
    }
}
