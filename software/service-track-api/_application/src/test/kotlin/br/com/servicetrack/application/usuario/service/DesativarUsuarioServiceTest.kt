package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

class DesativarUsuarioServiceTest {

    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val jwt = mockk<JwtPort>()
    private val service = DesativarUsuarioService(usuarioRepository, jwt)

    private val clienteId = UsuarioId.gerar()
    private val mecanicoId = UsuarioId.gerar()
    private val outroMecanicoId = UsuarioId.gerar()

    private fun buildUsuario(id: UsuarioId, roles: Set<Role>): Usuario = Usuario.reconstituir(
        id = id,
        nome = "Usuário Teste",
        email = Email("usuario@email.com"),
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
    fun `deve desativar proprio usuario quando cliente faz a requisicao`() {
        val cliente = buildUsuario(clienteId, setOf(Role.CLIENTE))

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        justRun { usuarioRepository.desativar(clienteId) }

        service.desativarUsuario(clienteId)

        verify(exactly = 1) { usuarioRepository.desativar(clienteId) }
    }

    @Test
    fun `deve desativar cliente quando mecanico faz a requisicao`() {
        val mecanico = buildUsuario(mecanicoId, setOf(Role.MECANICO))
        val cliente = buildUsuario(clienteId, setOf(Role.CLIENTE))

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        justRun { usuarioRepository.desativar(clienteId) }

        service.desativarUsuario(clienteId)

        verify(exactly = 1) { usuarioRepository.desativar(clienteId) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando cliente tenta desativar outro usuario`() {
        val clienteSolicitante = buildUsuario(clienteId, setOf(Role.CLIENTE))
        val outroCliente = buildUsuario(mecanicoId, setOf(Role.CLIENTE))

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns clienteSolicitante
        every { usuarioRepository.buscarPorId(mecanicoId) } returns outroCliente

        assertThrows<OperacaoNegadaException> {
            service.desativarUsuario(mecanicoId)
        }

        verify(exactly = 0) { usuarioRepository.desativar(mecanicoId) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando mecanico tenta desativar outro mecanico`() {
        val mecanico = buildUsuario(mecanicoId, setOf(Role.MECANICO))
        val outroMecanico = buildUsuario(outroMecanicoId, setOf(Role.MECANICO))

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(outroMecanicoId) } returns outroMecanico

        assertThrows<OperacaoNegadaException> {
            service.desativarUsuario(outroMecanicoId)
        }

        verify(exactly = 0) { usuarioRepository.desativar(mecanicoId) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando solicitante nao encontrado`() {
        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.desativarUsuario(clienteId)
        }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando alvo nao encontrado`() {
        val mecanico = buildUsuario(mecanicoId, setOf(Role.MECANICO))

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(clienteId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.desativarUsuario(clienteId)
        }
    }
}
