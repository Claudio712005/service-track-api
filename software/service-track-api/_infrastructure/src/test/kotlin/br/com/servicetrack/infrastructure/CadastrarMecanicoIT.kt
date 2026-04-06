package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

@QuarkusTest
class CadastrarMecanicoIT {

    // CPFs válidos (dígitos verificadores corretos):
    // 11144477735, 00000000191, 12345678909

    @Test
    fun `deve cadastrar mecanico com sucesso e retornar 201`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "João Mecânico",
                  "email": "mecanico.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11999999998",
                  "cpf": "11144477735",
                  "dataNascimento": "1985-03-20",
                  "nivel": "JUNIOR",
                  "valorHora": 50.00
                }
                """.trimIndent()
            )
        .`when`()
            .post("/mecanicos")
        .then()
            .statusCode(201)
            .body("usuarioId", notNullValue())
            .body("nome", equalTo("João Mecânico"))
            .body("email", equalTo("mecanico.it@email.com"))
            .body("nivel", equalTo("JUNIOR"))
            .body("valorHora", equalTo(50.0f))
            .body("ativo", equalTo(true))
    }

    @Test
    fun `deve retornar 409 quando cpf ja estiver cadastrado`() {
        val body = """
            {
              "nome": "Mecânico Original",
              "email": "mecanico.original@email.com",
              "senha": "#Tiee123456",
              "telefone": "11988887777",
              "cpf": "00000000191",
              "dataNascimento": "1985-03-20",
              "nivel": "PLENO",
              "valorHora": 80.00
            }
        """.trimIndent()

        given().contentType(ContentType.JSON).body(body).post("/mecanicos").then().statusCode(201)

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Duplicado",
                  "email": "outro.mecanico@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11977776666",
                  "cpf": "00000000191",
                  "dataNascimento": "1985-03-20",
                  "nivel": "JUNIOR",
                  "valorHora": 50.00
                }
                """.trimIndent()
            )
        .`when`()
            .post("/mecanicos")
        .then()
            .statusCode(409)
    }

    @Test
    fun `deve retornar 400 quando valor hora for zero`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Valor Inválido",
                  "email": "valor.invalido@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11966665555",
                  "cpf": "12345678909",
                  "dataNascimento": "1985-03-20",
                  "nivel": "JUNIOR",
                  "valorHora": 0
                }
                """.trimIndent()
            )
        .`when`()
            .post("/mecanicos")
        .then()
            .statusCode(400)
    }
}
