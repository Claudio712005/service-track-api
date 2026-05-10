package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class ServicoIT {

    private lateinit var tokenMecanico: String

    @BeforeEach
    fun setup() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Servico IT",
                  "email": "mecanico.servico.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11977778888",
                  "cpf": "29819930030",
                  "dataNascimento": "1985-03-15",
                  "nivel": "SENIOR",
                  "valorHora": 120.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        tokenMecanico = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "mecanico.servico.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token")
    }

    @Test
    fun `deve criar servico e retornar 201`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nomeServico": "Troca de Óleo",
                  "descricaoServico": "Substituição do óleo do motor e filtro",
                  "valorReferencia": 150.00
                }
                """.trimIndent()
            )
        .`when`()
            .post("/servicos")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nomeServico", equalTo("Troca de Óleo"))
            .body("descricaoServico", equalTo("Substituição do óleo do motor e filtro"))
            .body("valorReferencia", equalTo(150.0f))
    }

    @Test
    fun `deve buscar servico por id e retornar 200`() {
        val servicoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nomeServico": "Alinhamento",
                  "descricaoServico": "Alinhamento e balanceamento",
                  "valorReferencia": 100.00
                }
                """.trimIndent()
            )
            .post("/servicos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/servicos/$servicoId")
        .then()
            .statusCode(200)
            .body("id", equalTo(servicoId))
            .body("nomeServico", equalTo("Alinhamento"))
    }

    @Test
    fun `deve listar servicos e retornar 200`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nomeServico": "Revisão Completa",
                  "descricaoServico": "Revisão de todos os componentes do veículo"
                }
                """.trimIndent()
            )
            .post("/servicos")
            .then()
            .statusCode(201)

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/servicos")
        .then()
            .statusCode(200)
    }

    @Test
    fun `deve atualizar servico e retornar 200`() {
        val servicoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nomeServico": "Troca de Pneu",
                  "descricaoServico": "Troca de pneu dianteiro"
                }
                """.trimIndent()
            )
            .post("/servicos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body("""{"nomeServico": "Troca de Pneu Traseiro", "valorReferencia": 80.00}""")
        .`when`()
            .put("/servicos/$servicoId")
        .then()
            .statusCode(200)
            .body("nomeServico", equalTo("Troca de Pneu Traseiro"))
            .body("valorReferencia", equalTo(80.0f))
    }

    @Test
    fun `deve remover servico e retornar 204`() {
        val servicoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nomeServico": "Serviço a Remover",
                  "descricaoServico": "Será removido no teste"
                }
                """.trimIndent()
            )
            .post("/servicos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .delete("/servicos/$servicoId")
        .then()
            .statusCode(204)

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/servicos/$servicoId")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"nomeServico": "X", "descricaoServico": "Y"}""")
        .`when`()
            .post("/servicos")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 404 quando servico nao encontrado`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/servicos/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404)
    }
}
