package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.usuario.dto.request.AtualizarUsuarioReqDTO
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
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

class AtualizarUsuarioServiceTest {

    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val jwt = mockk<JwtPort>()
    private val service = AtualizarUsuarioService(usuarioRepository, jwt)

    private val clienteId = UsuarioId.gerar()
    private val mecanicoId = UsuarioId.gerar()
    private val outroMecanicoId = UsuarioId.gerar()

    private val req = AtualizarUsuarioReqDTO(
        nome = "Nome Atualizado",
        email = "atualizado@email.com",
        telefone = "11988887777"
    )

    private fun buildUsuario(id: UsuarioId, roles: Set<Role>): Usuario = Usuario.reconstituir(
        id = id,
        nome = "Nome Original",
        email = Email("original@email.com"),
        senhaHash = Senha.deHash("\$2a\$10\$hashFake"),
        dataNascimento = LocalDate.of(1990, 1, 1),
        telefone = Telefone("11999999999"),
        cpf = Cpf("52998224725"),
        ativo = true,
        roles = roles,
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now()
    )

    @Test
    fun `deve atualizar proprio usuario quando cliente faz a requisicao`() {
        val cliente = buildUsuario(clienteId, setOf(Role.CLIENTE))

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { usuarioRepository.existeEmailEmOutroUsuario(req.email, clienteId) } returns false
        justRun { usuarioRepository.atualizar(any()) }

        val resultado = service.atualizarUsuario(clienteId, req)

        assertEquals("Nome Atualizado", resultado.nome)
        assertEquals("atualizado@email.com", resultado.email)
        verify(exactly = 1) { usuarioRepository.atualizar(any()) }
    }

    @Test
    fun `deve atualizar cliente quando mecanico faz a requisicao`() {
        val mecanico = buildUsuario(mecanicoId, setOf(Role.MECANICO))
        val cliente = buildUsuario(clienteId, setOf(Role.CLIENTE))

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { usuarioRepository.existeEmailEmOutroUsuario(req.email, clienteId) } returns false
        justRun { usuarioRepository.atualizar(any()) }

        val resultado = service.atualizarUsuario(clienteId, req)

        assertEquals("Nome Atualizado", resultado.nome)
        verify(exactly = 1) { usuarioRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando cliente tenta atualizar outro usuario`() {
        val clienteSolicitante = buildUsuario(clienteId, setOf(Role.CLIENTE))
        val outroCliente = buildUsuario(mecanicoId, setOf(Role.CLIENTE))

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns clienteSolicitante
        every { usuarioRepository.buscarPorId(mecanicoId) } returns outroCliente

        assertThrows<OperacaoNegadaException> {
            service.atualizarUsuario(mecanicoId, req)
        }

        verify(exactly = 0) { usuarioRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando mecanico tenta atualizar outro mecanico`() {
        val mecanico = buildUsuario(mecanicoId, setOf(Role.MECANICO))
        val outroMecanico = buildUsuario(outroMecanicoId, setOf(Role.MECANICO))

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(outroMecanicoId) } returns outroMecanico

        assertThrows<OperacaoNegadaException> {
            service.atualizarUsuario(outroMecanicoId, req)
        }

        verify(exactly = 0) { usuarioRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando solicitante nao encontrado`() {
        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.atualizarUsuario(clienteId, req)
        }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando alvo nao encontrado`() {
        val mecanico = buildUsuario(mecanicoId, setOf(Role.MECANICO))

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(clienteId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.atualizarUsuario(clienteId, req)
        }
    }
}
