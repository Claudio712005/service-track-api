package br.com.servicetrack.infrastructure

import br.com.servicetrack.infrastructure.support.TokenTeste

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class DesativarClienteIT {

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
                  "nome": "Cliente Aut Desativar IT",
                  "email": "cliente.aut.desativar.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900003333",
                  "cpf": "11010101005",
                  "dataNascimento": "1991-07-15"
                }
                """.trimIndent()
            )
            .post("/clientes")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Aut Desativar IT",
                  "email": "mecanico.aut.desativar.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900004444",
                  "cpf": "12020202085",
                  "dataNascimento": "1982-11-05",
                  "nivel": "JUNIOR",
                  "valorHora": 60.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")
        tokenCliente = TokenTeste.para("cliente.aut.desativar.it@email.com")
        clienteId = TokenTeste.idDe("cliente.aut.desativar.it@email.com")
        tokenMecanico = TokenTeste.para("mecanico.aut.desativar.it@email.com")
        mecanicoId = TokenTeste.idDe("mecanico.aut.desativar.it@email.com")
    }

    @Test
    fun `deve desativar cliente como mecanico e retornar 204`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Alvo Desativar IT",
                  "email": "cliente.alvo.desativar.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900005555",
                  "cpf": "13030303063",
                  "dataNascimento": "1995-03-10"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val clienteAlvoId = TokenTeste.idDe("cliente.alvo.desativar.it@email.com")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .delete("/clientes/$clienteAlvoId")
        .then()
            .statusCode(204)
    }

    @Test
    fun `deve impedir login apos desativacao`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Block Login IT",
                  "email": "cliente.block.login.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900006666",
                  "cpf": "14040404041",
                  "dataNascimento": "1990-06-20"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val blockId = TokenTeste.idDe("cliente.block.login.it@email.com")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
            .delete("/clientes/$blockId")
            .then().statusCode(204)

    }

    @Test
    fun `deve retornar 403 quando cliente tenta desativar outro usuario`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .delete("/clientes/$mecanicoId")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 403 quando mecanico tenta desativar outro mecanico`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Segundo Mecânico Desativar IT",
                  "email": "segundo.mecanico.desativar.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900007777",
                  "cpf": "15050505020",
                  "dataNascimento": "1978-03-22",
                  "nivel": "JUNIOR",
                  "valorHora": 50.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        val segundoMecanicoId = TokenTeste.idDe("segundo.mecanico.desativar.it@email.com")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .delete("/clientes/$segundoMecanicoId")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao desativar usuario`() {
        given()
        .`when`()
            .delete("/clientes/$clienteId")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 404 quando usuario nao existe`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .delete("/clientes/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404)
    }
}
