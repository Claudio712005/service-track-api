package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.*
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ListarVeiculosServiceTest {

    private lateinit var repository: VeiculoRepositoryPort
    private lateinit var usuarioRepository: UsuarioRepositoryPort
    private lateinit var jwt: JwtPort
    private lateinit var service: ListarVeiculosService

    @BeforeEach
    fun setup() {
        repository = mockk()
        usuarioRepository = mockk()
        jwt = mockk()
        service = ListarVeiculosService(repository, usuarioRepository, jwt)
    }

    @Test
    fun `deve listar todos veiculos quando usuario for mecanico`() {
        val usuarioId = UsuarioId.gerar()

        val usuario = Usuario.criarMecanico(
            nome = "Mecânico",
            email = Email("mecanico@email.com"),
            senha = Senha.criar("@Senha123"),
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = Telefone("11999999999"),
            cpf = Cpf("72732437018")
        )

        val veiculo1 = Veiculo.criar(
            usuarioId,
            placa = Placa("ABC1D23"),
            marca = "Toyota",
            modelo = "Corolla",
            ano = 2020
        )

        val veiculo2 = Veiculo.criar(
            usuarioId,
            placa = Placa("XYZ9K88"),
            marca = "Honda",
            modelo = "Civic",
            ano = 2022
        )

        every { jwt.getUsuarioId() } returns usuarioId
        every { usuarioRepository.buscarPorId(usuarioId) } returns usuario
        every { repository.listarTodos() } returns listOf(veiculo1, veiculo2)

        val resultado = service.listarVeiculos()

        assertEquals(2, resultado.size)

        verify { jwt.getUsuarioId() }
        verify { usuarioRepository.buscarPorId(usuarioId) }
        verify { repository.listarTodos() }
        verify(exactly = 0) { repository.listarPorProprietario(any()) }
    }

    @Test
    fun `deve listar veiculos do proprietario quando usuario nao for mecanico`() {
        val usuarioId = UsuarioId.gerar()

        val usuario = Usuario.criarCliente(
            nome = "Cliente",
            email = Email("cliente@email.com"),
            senha = Senha.criar("@Senha123"),
            dataNascimento = LocalDate.of(1995, 5, 5),
            telefone = Telefone("11988888888"),
            cpf = Cpf("40947646078")
        )

        val veiculo = Veiculo.criar(
            usuarioId,
            placa = Placa("ABC1D23"),
            marca = "Toyota",
            modelo = "Corolla",
            ano = 2020
        )

        every { jwt.getUsuarioId() } returns usuarioId
        every { usuarioRepository.buscarPorId(usuarioId) } returns usuario
        every { repository.listarPorProprietario(usuarioId) } returns listOf(veiculo)

        val resultado = service.listarVeiculos()

        assertEquals(1, resultado.size)

        verify { jwt.getUsuarioId() }
        verify { usuarioRepository.buscarPorId(usuarioId) }
        verify { repository.listarPorProprietario(usuarioId) }
        verify(exactly = 0) { repository.listarTodos() }
    }

    @Test
    fun `deve lancar excecao quando usuario nao encontrado`() {
        val usuarioId = UsuarioId.gerar()

        every { jwt.getUsuarioId() } returns usuarioId
        every { usuarioRepository.buscarPorId(usuarioId) } returns null

        val exception = assertThrows(EntidadeNaoEncontradaException::class.java) {
            service.listarVeiculos()
        }

        assertTrue(exception.message!!.contains("Usuario"))

        verify { jwt.getUsuarioId() }
        verify { usuarioRepository.buscarPorId(usuarioId) }
        verify(exactly = 0) { repository.listarTodos() }
        verify(exactly = 0) { repository.listarPorProprietario(any()) }
    }
}