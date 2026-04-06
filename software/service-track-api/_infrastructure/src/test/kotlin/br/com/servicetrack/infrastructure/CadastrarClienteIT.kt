package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

@QuarkusTest
class CadastrarClienteIT {

    @Test
    fun `deve cadastrar cliente com sucesso e retornar 201`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cláudio da Silva",
                  "email": "cliente.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11999999999",
                  "cpf": "54927170063",
                  "dataNascimento": "1990-01-15"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/clientes")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo("Cláudio da Silva"))
            .body("email", equalTo("cliente.it@email.com"))
            .body("ativo", equalTo(true))
    }

    @Test
    fun `deve retornar 409 quando cpf ja estiver cadastrado`() {
        val body = """
            {
              "nome": "Cláudio Duplicado",
              "email": "duplicado.it@email.com",
              "senha": "#Tiee123456",
              "telefone": "11988888888",
              "cpf": "14716682072",
              "dataNascimento": "1990-01-15"
            }
        """.trimIndent()

        given().contentType(ContentType.JSON).body(body).post("/clientes").then().statusCode(201)

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Outro Nome",
                  "email": "outro.email@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11977777777",
                  "cpf": "14716682072",
                  "dataNascimento": "1990-01-15"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/clientes")
        .then()
            .statusCode(409)
    }

    @Test
    fun `deve retornar 400 quando senha nao atender a politica`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Senha Fraca",
                  "email": "senhafraca.it@email.com",
                  "senha": "fraca",
                  "telefone": "11966666666",
                  "cpf": "09322929000",
                  "dataNascimento": "1990-01-15"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/clientes")
        .then()
            .statusCode(400)
    }
}
