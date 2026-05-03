package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.exception.CredenciaisInvalidasException
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.usuario.dto.request.ResetarSenhaReqDTO
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

class ResetarSenhaServiceTest {

    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val criptografia = mockk<CriptografiaPort>()
    private val jwt = mockk<JwtPort>()
    private val service = ResetarSenhaService(usuarioRepository, criptografia, jwt)

    private val usuarioId = UsuarioId.gerar()

    private fun buildUsuario(ativo: Boolean = true): Usuario = Usuario.reconstituir(
        id = usuarioId,
        nome = "Usuário Teste",
        email = Email("usuario@email.com"),
        senhaHash = Senha.deHash("\$2a\$10\$hashFake"),
        dataNascimento = LocalDate.of(1990, 1, 1),
        telefone = Telefone("11999999999"),
        cpf = Cpf("52998224725"),
        ativo = ativo,
        roles = setOf(Role.CLIENTE),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now()
    )

    @Test
    fun `deve resetar senha com sucesso`() {
        val usuario = buildUsuario()
        val req = ResetarSenhaReqDTO(
            senhaAtual = "Senha@123",
            novaSenha = "NovaSenha@456",
            confirmacaoNovaSenha = "NovaSenha@456"
        )

        every { jwt.getUsuarioId() } returns usuarioId
        every { usuarioRepository.buscarPorId(usuarioId) } returns usuario
        every { criptografia.comparar("\$2a\$10\$hashFake", "Senha@123") } returns true
        every { criptografia.criptografar("NovaSenha@456") } returns "\$2a\$10\$novoHash"
        justRun { usuarioRepository.atualizar(any()) }

        service.resetarSenha(req)

        verify(exactly = 1) { usuarioRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar CredenciaisInvalidasException quando senha atual incorreta`() {
        val usuario = buildUsuario()
        val req = ResetarSenhaReqDTO(
            senhaAtual = "SenhaErrada@123",
            novaSenha = "NovaSenha@456",
            confirmacaoNovaSenha = "NovaSenha@456"
        )

        every { jwt.getUsuarioId() } returns usuarioId
        every { usuarioRepository.buscarPorId(usuarioId) } returns usuario
        every { criptografia.comparar("\$2a\$10\$hashFake", "SenhaErrada@123") } returns false

        assertThrows<CredenciaisInvalidasException> {
            service.resetarSenha(req)
        }

        verify(exactly = 0) { usuarioRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando nova senha e confirmacao nao conferem`() {
        val usuario = buildUsuario()
        val req = ResetarSenhaReqDTO(
            senhaAtual = "Senha@123",
            novaSenha = "NovaSenha@456",
            confirmacaoNovaSenha = "SenhaDiferente@789"
        )

        every { jwt.getUsuarioId() } returns usuarioId
        every { usuarioRepository.buscarPorId(usuarioId) } returns usuario
        every { criptografia.comparar("\$2a\$10\$hashFake", "Senha@123") } returns true

        assertThrows<DomainException> {
            service.resetarSenha(req)
        }

        verify(exactly = 0) { usuarioRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando nova senha nao atende a politica`() {
        val usuario = buildUsuario()
        val req = ResetarSenhaReqDTO(
            senhaAtual = "Senha@123",
            novaSenha = "fraca",
            confirmacaoNovaSenha = "fraca"
        )

        every { jwt.getUsuarioId() } returns usuarioId
        every { usuarioRepository.buscarPorId(usuarioId) } returns usuario
        every { criptografia.comparar("\$2a\$10\$hashFake", "Senha@123") } returns true

        assertThrows<DomainException> {
            service.resetarSenha(req)
        }

        verify(exactly = 0) { usuarioRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando usuario nao encontrado`() {
        val req = ResetarSenhaReqDTO(
            senhaAtual = "Senha@123",
            novaSenha = "NovaSenha@456",
            confirmacaoNovaSenha = "NovaSenha@456"
        )

        every { jwt.getUsuarioId() } returns usuarioId
        every { usuarioRepository.buscarPorId(usuarioId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.resetarSenha(req)
        }
    }
}
