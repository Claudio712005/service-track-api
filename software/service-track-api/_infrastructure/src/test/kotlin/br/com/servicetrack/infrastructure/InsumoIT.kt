package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class InsumoIT {

    private lateinit var tokenMecanico: String

    @BeforeEach
    fun setup() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Insumo IT",
                  "email": "mecanico.insumo.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11966665555",
                  "cpf": "66417722090",
                  "dataNascimento": "1983-09-10",
                  "nivel": "PLENO",
                  "valorHora": 95.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        tokenMecanico = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "mecanico.insumo.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token")
    }

    @Test
    fun `deve criar insumo e retornar 201`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nome": "Filtro de Óleo",
                  "descricao": "Filtro de óleo para motor 1.0",
                  "custo": 25.50,
                  "qtdEstoqueInicial": 10,
                  "estoqueMinimo": 2
                }
                """.trimIndent()
            )
        .`when`()
            .post("/insumos")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo("Filtro de Óleo"))
            .body("custo", equalTo(25.5f))
            .body("qtdEstoque", equalTo(10))
            .body("estoqueMinimo", equalTo(2))
    }

    @Test
    fun `deve buscar insumo por id e retornar 200`() {
        val insumoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nome": "Pastilha de Freio",
                  "descricao": "Pastilha dianteira",
                  "custo": 89.90
                }
                """.trimIndent()
            )
            .post("/insumos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/insumos/$insumoId")
        .then()
            .statusCode(200)
            .body("id", equalTo(insumoId))
            .body("nome", equalTo("Pastilha de Freio"))
    }

    @Test
    fun `deve listar insumos e retornar 200`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nome": "Vela de Ignição",
                  "descricao": "Vela para motor flex",
                  "custo": 15.00
                }
                """.trimIndent()
            )
            .post("/insumos")
            .then()
            .statusCode(201)

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/insumos")
        .then()
            .statusCode(200)
    }

    @Test
    fun `deve atualizar insumo e retornar 200`() {
        val insumoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nome": "Fluido de Freio",
                  "descricao": "DOT 4",
                  "custo": 30.00
                }
                """.trimIndent()
            )
            .post("/insumos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body("""{"custo": 35.00, "estoqueMinimo": 5}""")
        .`when`()
            .put("/insumos/$insumoId")
        .then()
            .statusCode(200)
            .body("custo", equalTo(35.0f))
            .body("estoqueMinimo", equalTo(5))
    }

    @Test
    fun `deve remover insumo e retornar 204`() {
        val insumoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nome": "Insumo a Remover",
                  "descricao": "Será removido no teste",
                  "custo": 10.00
                }
                """.trimIndent()
            )
            .post("/insumos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .delete("/insumos/$insumoId")
        .then()
            .statusCode(204)

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/insumos/$insumoId")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"nome": "X", "descricao": "Y", "custo": 1.0}""")
        .`when`()
            .post("/insumos")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 404 quando insumo nao encontrado`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/insumos/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404)
    }
}
