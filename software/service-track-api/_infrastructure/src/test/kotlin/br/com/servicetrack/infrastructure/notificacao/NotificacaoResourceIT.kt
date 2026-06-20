package br.com.servicetrack.infrastructure.notificacao

import br.com.servicetrack.application.notificacao.dto.EnfileirarNotificacaoCommand
import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class NotificacaoResourceIT {

    @Inject
    lateinit var enfileirarUseCase: EnfileirarNotificacaoUseCase

    private lateinit var tokenCliente: String
    private lateinit var clienteId: String
    private lateinit var tokenMecanico: String
    private lateinit var notificacaoId: String

    companion object {
        private var initialized = false
        private var cachedTokenCliente = ""
        private var cachedClienteId = ""
        private var cachedTokenMecanico = ""
        private var cachedNotificacaoId = ""
    }

    @BeforeEach
    fun setup() {
        if (initialized) {
            tokenCliente = cachedTokenCliente
            clienteId = cachedClienteId
            tokenMecanico = cachedTokenMecanico
            notificacaoId = cachedNotificacaoId
            return
        }

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Cliente Notif IT",
                  "email": "cliente.notif.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11966661111",
                  "cpf": "46890615212",
                  "dataNascimento": "1990-06-15"
                }
                """.trimIndent()
            )
            .post("/clientes")

        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "nome": "Mecanico Notif IT",
                  "email": "mecanico.notif.it@email.com",
                  "senha": "#Tiee123456",
                  "telefone": "11966662222",
                  "cpf": "28317520862",
                  "dataNascimento": "1985-03-20",
                  "nivel": "PLENO",
                  "valorHora": 80.00
                }
                """.trimIndent()
            )
            .post("/mecanicos")

        val loginCliente = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "cliente.notif.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        tokenCliente = loginCliente.getString("token")
        clienteId = loginCliente.getString("usuarioId")

        val loginMecanico = given()
            .contentType(ContentType.JSON)
            .body("""{"email": "mecanico.notif.it@email.com", "senha": "#Tiee123456"}""")
            .post("/autenticacao")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        tokenMecanico = loginMecanico.getString("token")

        notificacaoId = QuarkusTransaction.requiringNew().call {
            enfileirarUseCase.executar(
                EnfileirarNotificacaoCommand(
                    assunto = AssuntoNotificacao("OS atualizada"),
                    titulo = TituloNotificacao("Status da OS alterado"),
                    descricao = DescricaoNotificacao("Sua OS mudou de status"),
                    tipoNotificacao = TipoNotificacao.EMAIL,
                    tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
                    destinatario = UsuarioId(clienteId),
                    variaveis = VariaveisTemplate.de(mapOf("os" to "123", "novoStatus" to "EM_DIAGNOSTICO", "nomeCliente" to "Cliente")),
                )
            ).value
        }

        cachedTokenCliente = tokenCliente
        cachedClienteId = clienteId
        cachedTokenMecanico = tokenMecanico
        cachedNotificacaoId = notificacaoId
        initialized = true
    }

    @Test
    fun `deve listar notificacoes do usuario autenticado`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/notificacoes")
        .then()
            .statusCode(200)
            .body("content.size()", greaterThanOrEqualTo(1))
            .body("page", equalTo(0))
            .body("size", equalTo(20))
            .body("total", greaterThanOrEqualTo(1))
    }

    @Test
    fun `deve retornar lista vazia para usuario sem notificacoes`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/notificacoes")
        .then()
            .statusCode(200)
            .body("content.size()", equalTo(0))
            .body("total", equalTo(0))
    }

    @Test
    fun `deve buscar notificacao por id`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/notificacoes/$notificacaoId")
        .then()
            .statusCode(200)
            .body("id", equalTo(notificacaoId))
            .body("titulo", notNullValue())
            .body("statusEnvio", equalTo("PENDENTE"))
    }

    @Test
    fun `deve retornar 403 ao buscar notificacao de outro usuario`() {
        given()
            .header("Authorization", "Bearer $tokenMecanico")
        .`when`()
            .get("/notificacoes/$notificacaoId")
        .then()
            .statusCode(403)
    }

    @Test
    fun `deve retornar 404 ao buscar notificacao inexistente`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/notificacoes/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404)
    }

    @Test
    fun `deve contar notificacoes nao lidas`() {
        given()
            .header("Authorization", "Bearer $tokenCliente")
        .`when`()
            .get("/notificacoes/nao-lidas/contagem")
        .then()
            .statusCode(200)
            .body("total", greaterThanOrEqualTo(0))
    }

    @Test
    fun `deve retornar 401 sem token`() {
        given()
        .`when`()
            .get("/notificacoes")
        .then()
            .statusCode(401)
    }
}
