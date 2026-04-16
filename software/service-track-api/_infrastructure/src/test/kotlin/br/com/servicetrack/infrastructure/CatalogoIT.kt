package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class CatalogoIT {

    private lateinit var tokenMecanico: String
    private lateinit var tokenCliente: String

    @BeforeEach
    fun setup() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Catalogo IT",
                  "email": "mecanico.catalogo.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900004444",
                  "cpf": "91473562864",
                  "dataNascimento": "1980-11-05",
                  "nivel": "PLENO",
                  "valorHora": 85.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Catalogo IT",
                  "email": "cliente.catalogo.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900005555",
                  "cpf": "47065199002",
                  "dataNascimento": "1993-03-14"
                }
                """.trimIndent()
            )
            .post("/clientes")

        tokenMecanico = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "mecanico.catalogo.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token")

        tokenCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.catalogo.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token")

        // Garante que existem dados no catálogo
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nomeServico": "Revisão Catalogo",
                  "descricaoServico": "Serviço para teste de catálogo"
                }
                """.trimIndent()
            )
            .post("/servicos")

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "nome": "Insumo Catalogo",
                  "descricao": "Insumo para teste de catálogo",
                  "custo": 20.00
                }
                """.trimIndent()
            )
            .post("/insumos")
    }

    @Test
    fun `deve listar servicos do catalogo como mecanico e retornar 200`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/catalogo/servicos")
        .then()
            .statusCode(200)
    }

    @Test
    fun `deve listar servicos do catalogo como cliente e retornar 200`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/catalogo/servicos")
        .then()
            .statusCode(200)
    }

    @Test
    fun `deve listar insumos do catalogo como mecanico e retornar 200`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/catalogo/insumos")
        .then()
            .statusCode(200)
    }

    @Test
    fun `deve listar insumos do catalogo como cliente e retornar 200`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/catalogo/insumos")
        .then()
            .statusCode(200)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao listar servicos do catalogo`() {
        given()
        .`when`()
            .get("/catalogo/servicos")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao listar insumos do catalogo`() {
        given()
        .`when`()
            .get("/catalogo/insumos")
        .then()
            .statusCode(401)
    }
}
