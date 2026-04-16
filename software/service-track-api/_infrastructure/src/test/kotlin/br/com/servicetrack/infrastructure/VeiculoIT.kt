package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class VeiculoIT {

    private lateinit var tokenCliente: String
    private lateinit var tokenMecanico: String
    private lateinit var clienteId: String

    @BeforeEach
    fun setup() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Veículo IT",
                  "email": "cliente.veiculo.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11911112222",
                  "cpf": "71428793860",
                  "dataNascimento": "1992-05-10"
                }
                """.trimIndent()
            )
            .post("/clientes")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico Veículo IT",
                  "email": "mecanico.veiculo.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11922223333",
                  "cpf": "10433218100",
                  "dataNascimento": "1985-07-20",
                  "nivel": "PLENO",
                  "valorHora": 90.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        val loginCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.veiculo.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        tokenCliente = loginCliente.getString("token")
        clienteId    = loginCliente.getString("usuarioId")

        tokenMecanico = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "mecanico.veiculo.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token")
    }

    @Test
    fun `deve cadastrar veiculo para si mesmo como cliente e retornar 201`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "placa": "ABC1D23",
                  "modelo": "Civic",
                  "marca": "Honda",
                  "ano": 2020,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/veiculos")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("placa", equalTo("ABC1D23"))
            .body("modelo", equalTo("Civic"))
            .body("marca", equalTo("Honda"))
            .body("ano", equalTo(2020))
            .body("proprietarioId", equalTo(clienteId))
    }

    @Test
    fun `deve permitir mecanico cadastrar veiculo para um cliente e retornar 201`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "placa": "XYZ9A87",
                  "modelo": "Corolla",
                  "marca": "Toyota",
                  "ano": 2022,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/veiculos")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("placa", equalTo("XYZ9A87"))
            .body("proprietarioId", equalTo(clienteId))
    }

    @Test
    fun `deve retornar 409 quando placa ja estiver cadastrada`() {
        val body = """
            {
              "placa": "DUP2E34",
              "modelo": "Onix",
              "marca": "Chevrolet",
              "ano": 2021,
              "proprietarioId": "$clienteId"
            }
        """.trimIndent()

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(body)
            .post("/veiculos")
            .then()
            .statusCode(201)

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(body)
        .`when`()
            .post("/veiculos")
        .then()
            .statusCode(409)
    }

    @Test
    fun `deve retornar 403 quando cliente tenta cadastrar veiculo para outro cliente`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Outro Cliente IT",
                  "email": "outro.cliente.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11933334444",
                  "cpf": "96001338914",
                  "dataNascimento": "1990-01-15"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val outroClienteId = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "outro.cliente.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("usuarioId")

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "placa": "FOR3B45",
                  "modelo": "Sandero",
                  "marca": "Renault",
                  "ano": 2019,
                  "proprietarioId": "$outroClienteId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/veiculos")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 404 quando proprietario nao encontrado`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "placa": "NOT5F67",
                  "modelo": "Compass",
                  "marca": "Jeep",
                  "ano": 2023,
                  "proprietarioId": "00000000-0000-0000-0000-000000000000"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/veiculos")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao cadastrar veiculo`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "placa": "SEM6G78",
                  "modelo": "Gol",
                  "marca": "Volkswagen",
                  "ano": 2018,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/veiculos")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve excluir veiculo como proprietario e retornar 204`() {
        val veiculoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "placa": "DEL7H89",
                  "modelo": "Pulse",
                  "marca": "Fiat",
                  "ano": 2023,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .delete("/veiculos/$veiculoId")
        .then()
            .statusCode(204)
    }

    @Test
    fun `deve permitir mecanico excluir veiculo de qualquer cliente e retornar 204`() {
        val veiculoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "placa": "MEC8I90",
                  "modelo": "Ka",
                  "marca": "Ford",
                  "ano": 2020,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .delete("/veiculos/$veiculoId")
        .then()
            .statusCode(204)
    }

    @Test
    fun `deve retornar 404 quando veiculo nao encontrado ao excluir`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .delete("/veiculos/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 403 quando cliente tenta excluir veiculo de outro cliente`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Outro Proprietario IT",
                  "email": "outro.proprietario.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11955556666",
                  "cpf": "08386379499",
                  "dataNascimento": "1988-11-20"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val tokenOutroCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "outro.proprietario.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token")

        val veiculoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "placa": "CLI9J01",
                  "modelo": "HB20",
                  "marca": "Hyundai",
                  "ano": 2021,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .header("Authorization", "Bearer $tokenOutroCliente")
        .`when`()
            .delete("/veiculos/$veiculoId")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao excluir veiculo`() {
        given()
        .`when`()
            .delete("/veiculos/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve buscar veiculo por id e retornar 200`() {
        val veiculoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "placa": "BSC1A11",
                  "modelo": "Fit",
                  "marca": "Honda",
                  "ano": 2019,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/veiculos/$veiculoId")
        .then()
            .statusCode(200)
            .body("id", equalTo(veiculoId))
            .body("placa", equalTo("BSC1A11"))
            .body("modelo", equalTo("Fit"))
            .body("marca", equalTo("Honda"))
            .body("ano", equalTo(2019))
    }

    @Test
    fun `deve buscar veiculo por id como mecanico e retornar 200`() {
        val veiculoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "placa": "BSC2B22",
                  "modelo": "Clio",
                  "marca": "Renault",
                  "ano": 2017,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/veiculos/$veiculoId")
        .then()
            .statusCode(200)
            .body("id", equalTo(veiculoId))
            .body("placa", equalTo("BSC2B22"))
    }

    @Test
    fun `deve retornar 404 ao buscar veiculo inexistente`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/veiculos/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao buscar veiculo`() {
        given()
        .`when`()
            .get("/veiculos/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve listar veiculos como mecanico e retornar 200`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "placa": "LST3C33",
                  "modelo": "Uno",
                  "marca": "Fiat",
                  "ano": 2015,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/veiculos")
        .then()
            .statusCode(200)
    }

    @Test
    fun `deve listar veiculos como cliente e retornar 200`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "placa": "LST4D44",
                  "modelo": "March",
                  "marca": "Nissan",
                  "ano": 2018,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)

        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/veiculos")
        .then()
            .statusCode(200)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao listar veiculos`() {
        given()
        .`when`()
            .get("/veiculos")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve atualizar veiculo como proprietario e retornar 200`() {
        val veiculoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "placa": "ATU5E55",
                  "modelo": "Palio",
                  "marca": "Fiat",
                  "ano": 2014,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body("""{"placa": "ATU5X55", "modelo": "Palio Sporting", "marca": "Fiat", "ano": 2015}""")
        .`when`()
            .put("/veiculos/$veiculoId")
        .then()
            .statusCode(200)
            .body("id", equalTo(veiculoId))
            .body("modelo", equalTo("Palio Sporting"))
            .body("ano", equalTo(2015))
    }

    @Test
    fun `deve atualizar veiculo como mecanico e retornar 200`() {
        val veiculoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "placa": "ATU6F66",
                  "modelo": "Fox",
                  "marca": "Volkswagen",
                  "ano": 2016,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body("""{"placa": "ATU6Y66", "modelo": "Fox Trend", "marca": "Volkswagen", "ano": 2016}""")
        .`when`()
            .put("/veiculos/$veiculoId")
        .then()
            .statusCode(200)
            .body("modelo", equalTo("Fox Trend"))
    }

    @Test
    fun `deve retornar 404 ao atualizar veiculo inexistente`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body("""{"placa": "ATU0X00", "modelo": "Modelo X", "marca": "Marca X", "ano": 2020}""")
        .`when`()
            .put("/veiculos/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 401 quando nao autenticado ao atualizar veiculo`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"modelo": "Modelo X"}""")
        .`when`()
            .put("/veiculos/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(401)
    }

    @Test
    fun `deve retornar 403 quando cliente tenta atualizar veiculo de outro cliente`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Outro Dono IT",
                  "email": "outro.dono.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11900001111",
                  "cpf": "58723401923",
                  "dataNascimento": "1991-06-25"
                }
                """.trimIndent()
            )
            .post("/clientes")

        val tokenOutroDono = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "outro.dono.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("token")

        val veiculoId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "placa": "ATU7G77",
                  "modelo": "Sandero",
                  "marca": "Renault",
                  "ano": 2020,
                  "proprietarioId": "$clienteId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenOutroDono")
            .body("""{"placa": "ATU7G77", "modelo": "Sandero Stepway", "marca": "Renault", "ano": 2020}""")
        .`when`()
            .put("/veiculos/$veiculoId")
        .then()
            .statusCode(403)
    }
}
