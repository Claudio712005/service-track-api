package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.exception.CredenciaisInvalidasException
import br.com.servicetrack.application.usuario.dto.request.LoginReqDTO
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

class LoginServiceTest {

    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val criptografia = mockk<CriptografiaPort>()
    private val jwt = mockk<JwtPort>()

    private val service = LoginService(usuarioRepository, criptografia, jwt)

    private val requisicao = LoginReqDTO(
        email = "clausilvaaraujo11@gmail.com",
        senha = "#Tiee123456"
    )

    private fun buildUsuario(ativo: Boolean): Usuario = Usuario.reconstituir(
        id = UsuarioId.gerar(),
        nome = "Cláudio",
        email = Email("clausilvaaraujo11@gmail.com"),
        senhaHash = Senha.deHash("\$2a\$10\$hashFake"),
        dataNascimento = LocalDate.of(2005, 1, 7),
        telefone = Telefone("11999999999"),
        cpf = Cpf("14716682072"),
        ativo = ativo,
        roles = setOf(Role.CLIENTE),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now()
    )

    @Test
    fun `deve realizar login com sucesso e retornar token`() {
        val usuarioAtivo = buildUsuario(ativo = true)
        every { usuarioRepository.buscarPorEmail(requisicao.email) } returns usuarioAtivo
        every { criptografia.comparar(any(), requisicao.senha) } returns true
        every { jwt.gerarToken(any(), any(), any()) } returns "jwt-token-gerado"

        val response = service.login(requisicao)

        assertNotNull(response)
        assertEquals("jwt-token-gerado", response.token)
        assertEquals(requisicao.email, response.email)
        assertNotNull(response.usuarioId)
        verify(exactly = 1) { jwt.gerarToken(any(), any(), any()) }
    }

    @Test
    fun `deve lancar CredenciaisInvalidasException quando usuario nao for encontrado`() {
        every { usuarioRepository.buscarPorEmail(requisicao.email) } returns null

        assertThrows<CredenciaisInvalidasException> {
            service.login(requisicao)
        }

        verify(exactly = 0) { jwt.gerarToken(any(), any(), any()) }
    }

    @Test
    fun `deve lancar CredenciaisInvalidasException quando senha estiver incorreta`() {
        val usuarioAtivo = buildUsuario(ativo = true)
        every { usuarioRepository.buscarPorEmail(requisicao.email) } returns usuarioAtivo
        every { criptografia.comparar(any(), requisicao.senha) } returns false

        assertThrows<CredenciaisInvalidasException> {
            service.login(requisicao)
        }

        verify(exactly = 0) { jwt.gerarToken(any(), any(), any()) }
    }

    @Test
    fun `deve lancar CredenciaisInvalidasException quando usuario estiver inativo`() {
        val usuarioInativo = buildUsuario(ativo = false)
        every { usuarioRepository.buscarPorEmail(requisicao.email) } returns usuarioInativo

        assertThrows<CredenciaisInvalidasException> {
            service.login(requisicao)
        }

        verify(exactly = 0) { criptografia.comparar(any(), any()) }
        verify(exactly = 0) { jwt.gerarToken(any(), any(), any()) }
    }
}
