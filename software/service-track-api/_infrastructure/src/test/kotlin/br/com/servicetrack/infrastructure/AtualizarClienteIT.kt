package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class AtualizarClienteIT {

    private lateinit var tokenCliente: String
    private lateinit var clienteId: String
    private lateinit var tokenMecanico: String
    private lateinit var mecanicoId: String

    @BeforeEach
    fun setup() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Aut Atualizar IT",
                  "email": "cliente.aut.atualizar.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900001111",
                  "cpf": "01010101099",
                  "dataNascimento": "1993-04-20"
                }
                """.trimIndent()
            )
            .post("/clientes")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Aut Atualizar IT",
                  "email": "mecanico.aut.atualizar.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900002222",
                  "cpf": "02020202077",
                  "dataNascimento": "1985-09-10",
                  "nivel": "JUNIOR",
                  "valorHora": 55.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        val loginCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.aut.atualizar.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath()

        tokenCliente = loginCliente.getString("token")
        clienteId = loginCliente.getString("usuarioId")

        val loginMecanico = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "mecanico.aut.atualizar.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath()

        tokenMecanico = loginMecanico.getString("token")
        mecanicoId = loginMecanico.getString("usuarioId")
    }

    @Test
    fun `deve atualizar proprio perfil como cliente e retornar 200`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Alvo Upd IT",
                  "email": "cliente.alvo.upd.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11911112222",
                  "cpf": "03040506013",
                  "dataNascimento": "1990-05-10"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val tokenAlvo = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.alvo.upd.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath()

        val alvoId = tokenAlvo.getString("usuarioId")
        val tokenAlvoStr = tokenAlvo.getString("token")

        given()
            .header("Authorization", "Bearer $tokenAlvoStr")
            .contentType(ContentType.JSON)
            .body("""{"nome": "Novo Nome Cliente", "email": "cliente.alvo.upd.novo.it@email.com", "telefone": "11988881111"}""")
        .`when`()
            .put("/clientes/$alvoId")
        .then()
            .statusCode(200)
            .body("nome", equalTo("Novo Nome Cliente"))
            .body("email", equalTo("cliente.alvo.upd.novo.it@email.com"))
    }

    @Test
    fun `deve atualizar cliente como mecanico e retornar 200`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Alvo Mec Upd IT",
                  "email": "cliente.alvo.mec.upd.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11922223333",
                  "cpf": "05050505011",
                  "dataNascimento": "1992-08-15"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val alvoId = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.alvo.mec.upd.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath().getString("usuarioId")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .contentType(ContentType.JSON)
            .body("""{"nome": "Cliente Atualizado pelo Mec", "email": "cliente.alvo.mec.upd.novo.it@email.com", "telefone": "11988882222"}""")
        .`when`()
            .put("/clientes/$alvoId")
        .then()
            .statusCode(200)
            .body("nome", equalTo("Cliente Atualizado pelo Mec"))
    }

    @Test
    fun `deve retornar 403 quando cliente tenta atualizar outro usuario`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
            .contentType(ContentType.JSON)
            .body("""{"nome": "Hacker", "email": "hacker.atualizar.it@email.com", "telefone": "11933334444"}""")
        .`when`()
            .put("/clientes/$mecanicoId")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 403 quando mecanico tenta atualizar outro mecanico`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Segundo Mecânico Atualizar IT",
                  "email": "segundo.mecanico.atualizar.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11944445555",
                  "cpf": "06060606008",
                  "dataNascimento": "1980-01-01",
                  "nivel": "SENIOR",
                  "valorHora": 100.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        val segundoMecanicoId = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "segundo.mecanico.atualizar.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then().statusCode(200)
            .extract().jsonPath().getString("usuarioId")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .contentType(ContentType.JSON)
            .body("""{"nome": "Hack", "email": "hack.atualizar.it@email.com", "telefone": "11955556666"}""")
        .`when`()
            .put("/clientes/$segundoMecanicoId")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao atualizar usuario`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"nome": "Sem Token", "email": "semtoken.atualizar.it@email.com", "telefone": "11966667777"}""")
        .`when`()
            .put("/clientes/$clienteId")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 404 quando usuario alvo nao existe`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .contentType(ContentType.JSON)
            .body("""{"nome": "Fantasma", "email": "fantasma.atualizar.it@email.com", "telefone": "11977778888"}""")
        .`when`()
            .put("/clientes/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404)
    }
}
