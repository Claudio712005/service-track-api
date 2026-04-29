package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class ResetarSenhaIT {

    private lateinit var tokenCliente: String

    @BeforeEach
    fun setup() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Reset Senha IT",
                  "email": "cliente.reset.senha.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900008888",
                  "cpf": "09090909044",
                  "dataNascimento": "1995-03-10"
                }
                """.trimIndent()
            )
            .post("/clientes")

        tokenCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.reset.senha.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath().getString("token")
    }

    @Test
    fun `deve resetar senha com sucesso e retornar 204`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Resetar Senha IT",
                  "email": "cliente.resetar.senha.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900009999",
                  "cpf": "16060606008",
                  "dataNascimento": "1993-01-10"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val token = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.resetar.senha.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath().getString("token")

        given()
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "senhaAtual": "#Tiee123456",
                  "novaSenha": "NovaSenha@789",
                  "confirmacaoNovaSenha": "NovaSenha@789"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/autenticacao/reset-senha")
        .then()
            .statusCode(204)
    }

    @Test
    fun `deve permitir login com nova senha apos reset`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Nova Senha IT",
                  "email": "cliente.nova.senha.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900010000",
                  "cpf": "17070707096",
                  "dataNascimento": "1994-09-20"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val token = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.nova.senha.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath().getString("token")

        given()
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "senhaAtual": "#Tiee123456",
                  "novaSenha": "NovaSenha@999",
                  "confirmacaoNovaSenha": "NovaSenha@999"
                }
                """.trimIndent()
            )
            .post("/autenticacao/reset-senha")
            .then().statusCode(204)

        given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.nova.senha.it@email.com", "senha": "NovaSenha@999"}""")
        .`when`()
            .post("/autenticacao")
        .then()
            .statusCode(200)
    }

    @Test
    fun `deve retornar 401 quando senha atual incorreta`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "senhaAtual": "SenhaErrada@000",
                  "novaSenha": "NovaSenha@789",
                  "confirmacaoNovaSenha": "NovaSenha@789"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/autenticacao/reset-senha")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 400 quando nova senha e confirmacao nao conferem`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "senhaAtual": "#Tiee123456",
                  "novaSenha": "NovaSenha@789",
                  "confirmacaoNovaSenha": "SenhaDiferente@000"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/autenticacao/reset-senha")
        .then()
            .statusCode(400)
    }

    @Test
    fun `deve retornar 400 quando nova senha nao atende a politica`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "senhaAtual": "#Tiee123456",
                  "novaSenha": "fraca",
                  "confirmacaoNovaSenha": "fraca"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/autenticacao/reset-senha")
        .then()
            .statusCode(400)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "senhaAtual": "#Tiee123456",
                  "novaSenha": "NovaSenha@789",
                  "confirmacaoNovaSenha": "NovaSenha@789"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/autenticacao/reset-senha")
        .then()
            .statusCode(401)
    }
}
