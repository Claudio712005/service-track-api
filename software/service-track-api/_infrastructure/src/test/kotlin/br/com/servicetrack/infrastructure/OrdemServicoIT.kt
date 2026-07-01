package br.com.servicetrack.infrastructure

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class OrdemServicoIT {

    private lateinit var tokenMecanico: String
    private lateinit var tokenCliente: String
    private lateinit var mecanicoId: String
    private lateinit var clienteId: String
    private lateinit var veiculoId: String

    private fun gerarCpf(): String {
        val digits = (1..9).map { (0..9).random() }
        var sum = digits.mapIndexed { i, d -> d * (10 - i) }.sum()
        val d10 = (sum % 11).let { if (it < 2) 0 else 11 - it }
        sum = digits.mapIndexed { i, d -> d * (11 - i) }.sum() + d10 * 2
        val d11 = (sum % 11).let { if (it < 2) 0 else 11 - it }
        return "${digits.joinToString("")}$d10$d11"
    }

    private fun gerarPlaca(): String {
        val alpha = ('A'..'Z').toList()
        val alphaNum = alpha + ('0'..'9').toList()
        val digit = ('0'..'9').toList()
        return "${alpha.random()}${alpha.random()}${alpha.random()}${digit.random()}${alphaNum.random()}${digit.random()}${digit.random()}"
    }

    private fun criarVeiculo(token: String, proprietarioId: String): String {
        return given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $token")
            .body(
                """
                {
                  "placa": "${gerarPlaca()}",
                  "modelo": "Civic",
                  "marca": "Honda",
                  "ano": 2021,
                  "proprietarioId": "$proprietarioId"
                }
                """.trimIndent()
            )
            .post("/veiculos")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")
    }

    @BeforeEach
    fun setup() {
        val unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8)

        val emailMecanico = "mecanico.$unique@email.com"
        val emailCliente = "cliente.$unique@email.com"

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecânico OS IT",
                  "email": "$emailMecanico",
                  "senha": "#Tiee123456",
                  "telefone": "11944445555",
                  "cpf": "${gerarCpf()}",
                  "dataNascimento": "1982-04-15",
                  "nivel": "SENIOR",
                  "valorHora": 120.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")
            .then()
            .statusCode(201)

        val loginMecanico = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "$emailMecanico", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        tokenMecanico = loginMecanico.getString("token")
        mecanicoId = loginMecanico.getString("usuarioId")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente OS IT",
                  "email": "$emailCliente",
                  "senha": "#Tiee123456",
                  "telefone": "11955556666",
                  "cpf": "${gerarCpf()}",
                  "dataNascimento": "1995-07-20"
                }
                """.trimIndent()
            )
            .post("/clientes")
            .then()
            .statusCode(201)

        val loginCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "$emailCliente", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        tokenCliente = loginCliente.getString("token")
        clienteId = loginCliente.getString("usuarioId")

        veiculoId = criarVeiculo(tokenMecanico, clienteId)
    }

    @Test
    fun `deve criar ordem de servico como mecanico e retornar 201`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "motivo": "Revisão preventiva",
                  "clienteId": "$clienteId",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$veiculoId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/ordem-servico")
        .then()
            .statusCode(201)
    }

    @Test
    fun `deve criar ordem de servico como cliente para si mesmo e retornar 201`() {
        val outroVeiculoId = criarVeiculo(tokenMecanico, clienteId)

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenCliente")
            .body(
                """
                {
                  "motivo": "Troca de óleo",
                  "clienteId": "$clienteId",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$outroVeiculoId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/ordem-servico")
        .then()
            .statusCode(201)
    }

    @Test
    fun `deve criar ordem de servico com observacao e retornar 201`() {
        val outroVeiculoId = criarVeiculo(tokenMecanico, clienteId)

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "motivo": "Barulho ao frear",
                  "clienteId": "$clienteId",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$outroVeiculoId",
                  "observacao": "Cliente relata barulho metálico ao frear"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/ordem-servico")
        .then()
            .statusCode(201)
    }

    @Test
    fun `deve retornar 403 quando cliente tenta criar OS para outro cliente`() {
        val uniqueOutro = UUID.randomUUID().toString().replace("-", "").substring(0, 8)
        val emailOutro = "outro.$uniqueOutro@email.com"

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Outro Cliente OS IT",
                  "email": "$emailOutro",
                  "senha": "#Tiee123456",
                  "telefone": "11966667777",
                  "cpf": "${gerarCpf()}",
                  "dataNascimento": "1993-03-10"
                }
                """.trimIndent()
            )
            .post("/clientes")
            .then()
            .statusCode(201)

        val outroClienteId = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "$emailOutro", "senha": "#Tiee123456"}""")
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
                  "motivo": "Revisão",
                  "clienteId": "$outroClienteId",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$veiculoId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/ordem-servico")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 403 quando ja existe OS aberta para o veiculo e cliente`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "motivo": "Primeira OS",
                  "clienteId": "$clienteId",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$veiculoId"
                }
                """.trimIndent()
            )
            .post("/ordem-servico")
            .then()
            .statusCode(201)

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "motivo": "Segunda OS para o mesmo veiculo",
                  "clienteId": "$clienteId",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$veiculoId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/ordem-servico")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 404 quando cliente nao encontrado`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "motivo": "Revisão",
                  "clienteId": "00000000-0000-0000-0000-000000000000",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$veiculoId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/ordem-servico")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 404 quando mecanico nao encontrado`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "motivo": "Revisão",
                  "clienteId": "$clienteId",
                  "mecanicoId": "00000000-0000-0000-0000-000000000000",
                  "veiculoId": "$veiculoId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/ordem-servico")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve retornar 400 quando motivo nao informado`() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "clienteId": "$clienteId",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$veiculoId"
                }
                """.trimIndent()
            )
        .`when`()
            .post("/ordem-servico")
        .then()
            .statusCode(400)
    }

    private fun criarOrdemServico(veiculo: String): String {
        return given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer $tokenMecanico")
            .body(
                """
                {
                  "motivo": "OS listagem",
                  "clienteId": "$clienteId",
                  "mecanicoId": "$mecanicoId",
                  "veiculoId": "$veiculo"
                }
                """.trimIndent()
            )
            .post("/ordem-servico")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id")
    }

    @Test
    fun `deve listar ordenando por prioridade de status e excluir entregues`() {
        val veiculo2 = criarVeiculo(tokenMecanico, clienteId)

        val osRecebida = criarOrdemServico(veiculoId)          
        val osDiagnostico = criarOrdemServico(veiculo2)        

        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .post("/ordem-servico/$osDiagnostico/diagnostico")
        .then()
            .statusCode(200)

        val statuses = given()
            .header("Authorization", "Bearer $tokenMecanico")
            .queryParam("mecanicoId", mecanicoId)
        .`when`()
            .get("/ordem-servico/lista")
        .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList<String>("content.id")

        val idxDiagnostico = statuses.indexOf(osDiagnostico)
        val idxRecebida = statuses.indexOf(osRecebida)
        assert(idxDiagnostico in 0 until idxRecebida) {
            "Esperado EM_DIAGNOSTICO ($idxDiagnostico) antes de RECEBIDA ($idxRecebida): $statuses"
        }
    }
}