package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class AtualizarMecanicoIT {

    private lateinit var tokenSenior: String
    private lateinit var mecanicoSeniorId: String
    private lateinit var tokenJunior: String
    private lateinit var tokenCliente: String
    private lateinit var mecanicoJuniorId: String

    @BeforeEach
    fun setup() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Sênior Atualiza IT",
                  "email": "senior.atualiza.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900004444",
                  "cpf": "77659513001",
                  "dataNascimento": "1975-06-10",
                  "nivel": "SENIOR",
                  "valorHora": 150.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Junior Alvo IT",
                  "email": "junior.alvo.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900005555",
                  "cpf": "26712349028",
                  "dataNascimento": "1998-02-14",
                  "nivel": "JUNIOR",
                  "valorHora": 50.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Atualiza IT",
                  "email": "cliente.atualiza.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900006666",
                  "cpf": "73653192080",
                  "dataNascimento": "1990-11-30"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val loginSenior = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "senior.atualiza.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath()

        tokenSenior = loginSenior.getString("token")
        mecanicoSeniorId = loginSenior.getString("usuarioId")

        val loginJunior = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "junior.alvo.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath()

        tokenJunior = loginJunior.getString("token")
        mecanicoJuniorId = loginJunior.getString("usuarioId")

        tokenCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.atualiza.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath().getString("token")
    }

    @Test
    fun  `deve atualizar mecanico com sucesso quando mecanico Senior faz a requisicao e retornar 200`() {
        given()
            .header("Authorization", "Bearer $tokenSenior")
            .contentType(ContentType.JSON)
            .body("""{"nivel": "PLENO", "valorHora": 90.00}""")
        .`when`()
            .put("/mecanicos/$mecanicoJuniorId")
        .then()
            .log().body()
            .statusCode(200)
            .body("usuarioId", equalTo(mecanicoJuniorId))
            .body("nivel", equalTo("PLENO"))
            .body("valorHora", equalTo(90.0f))
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao atualizar mecanico`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"nivel": "PLENO", "valorHora": 90.00}""")
        .`when`()
            .put("/mecanicos/$mecanicoJuniorId")
        .then()
            .log().body()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 403 quando cliente tenta atualizar mecanico`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
            .contentType(ContentType.JSON)
            .body("""{"nivel": "PLENO", "valorHora": 90.00}""")
        .`when`()
            .put("/mecanicos/$mecanicoJuniorId")
        .then()
            .log().body()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 403 quando mecanico Junior tenta atualizar outro mecanico`() {
        given()
            .header("Authorization", "Bearer $tokenJunior")
            .contentType(ContentType.JSON)
            .body("""{"nivel": "PLENO", "valorHora": 90.00}""")
        .`when`()
            .put("/mecanicos/$mecanicoSeniorId")
        .then()
            .log().body()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 404 ao atualizar mecanico inexistente`() {
        given()
            .header("Authorization", "Bearer $tokenSenior")
            .contentType(ContentType.JSON)
            .body("""{"nivel": "PLENO", "valorHora": 90.00}""")
        .`when`()
            .put("/mecanicos/00000000-0000-0000-0000-000000000000")
        .then()
            .log().body()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 400 quando valor hora for invalido`() {
        given()
            .header("Authorization", "Bearer $tokenSenior")
            .contentType(ContentType.JSON)
            .body("""{"nivel": "PLENO", "valorHora": 0.00}""")
        .`when`()
            .put("/mecanicos/$mecanicoJuniorId")
        .then()
            .log().body()
            .statusCode(400)
    }
}
