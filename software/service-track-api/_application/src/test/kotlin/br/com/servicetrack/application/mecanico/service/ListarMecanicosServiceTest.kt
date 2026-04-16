package br.com.servicetrack.application.mecanico.service

import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.mecanico.Mecanico
import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import br.com.servicetrack.domain.mecanico.vo.NivelMecanico
import br.com.servicetrack.domain.mecanico.vo.ValorHora
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class ListarMecanicosServiceTest {

    private lateinit var mecanicoRepository: MecanicoRepositoryPort
    private lateinit var usuarioRepository: UsuarioRepositoryPort
    private lateinit var service: ListarMecanicosService

    @BeforeEach
    fun setup() {
        mecanicoRepository = mockk()
        usuarioRepository = mockk()
        service = ListarMecanicosService(mecanicoRepository, usuarioRepository)
    }

    @Test
    fun `deve listar mecanicos com sucesso`() {
        val usuarioId1 = UsuarioId.gerar()
        val usuarioId2 = UsuarioId.gerar()

        val mecanico1 = Mecanico.criar(
            usuarioId = usuarioId1,
            valorHora = ValorHora(BigDecimal("100.00")),
            nivel = NivelMecanico.criar(NivelMecanicoEnum.PLENO)
        )

        val mecanico2 = Mecanico.criar(
            usuarioId = usuarioId2,
            valorHora = ValorHora(BigDecimal("150.00")),
            nivel = NivelMecanico.criar(NivelMecanicoEnum.SENIOR)
        )

        val usuario1 = Usuario.criarMecanico(
            nome = "João Silva",
            email = Email("joao@email.com"),
            senha = Senha.criar("@Senha123"),
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = Telefone("11999999999"),
            cpf = Cpf("72732437018")
        )

        val usuario2 = Usuario.criarMecanico(
            nome = "Maria Souza",
            email = Email("maria@email.com"),
            senha = Senha.criar("@Senha123"),
            dataNascimento = LocalDate.of(1992, 2, 2),
            telefone = Telefone("11988888888"),
            cpf = Cpf("10136451039")
        )

        every { mecanicoRepository.listarTodos() } returns listOf(mecanico1, mecanico2)
        every { usuarioRepository.buscarPorId(usuarioId1) } returns usuario1
        every { usuarioRepository.buscarPorId(usuarioId2) } returns usuario2

        val resultado = service.listarMecanicos()

        assertEquals(2, resultado.size)
        assertEquals(usuario1.id.valor, resultado[0].usuarioId)
        assertEquals(usuario2.id.valor, resultado[1].usuarioId)

        verify { mecanicoRepository.listarTodos() }
        verify { usuarioRepository.buscarPorId(usuarioId1) }
        verify { usuarioRepository.buscarPorId(usuarioId2) }
    }

    @Test
    fun `deve ignorar mecanico quando usuario nao encontrado`() {
        val usuarioId1 = UsuarioId.gerar()
        val usuarioId2 = UsuarioId.gerar()

        val mecanico1 = Mecanico.criar(
            usuarioId = usuarioId1,
            valorHora = ValorHora(BigDecimal("100.00")),
            nivel = NivelMecanico.criar(NivelMecanicoEnum.PLENO)
        )

        val mecanico2 = Mecanico.criar(
            usuarioId = usuarioId2,
            valorHora = ValorHora(BigDecimal("150.00")),
            nivel = NivelMecanico.criar(NivelMecanicoEnum.SENIOR)
        )

        val usuario1 = Usuario.criarMecanico(
            nome = "João Silva",
            email = Email("joao@email.com"),
            senha = Senha.criar("@Senha123"),
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = Telefone("11999999999"),
            cpf = Cpf("72732437018")
        )

        every { mecanicoRepository.listarTodos() } returns listOf(mecanico1, mecanico2)
        every { usuarioRepository.buscarPorId(usuarioId1) } returns usuario1
        every { usuarioRepository.buscarPorId(usuarioId2) } returns null

        val resultado = service.listarMecanicos()

        assertEquals(1, resultado.size)
        assertEquals(usuario1.id.valor, resultado[0].usuarioId)

        verify { mecanicoRepository.listarTodos() }
        verify { usuarioRepository.buscarPorId(usuarioId1) }
        verify { usuarioRepository.buscarPorId(usuarioId2) }
    }

    @Test
    fun `deve retornar lista vazia quando nao houver mecanicos`() {
        every { mecanicoRepository.listarTodos() } returns emptyList()

        val resultado = service.listarMecanicos()

        assertTrue(resultado.isEmpty())

        verify { mecanicoRepository.listarTodos() }
        verify(exactly = 0) { usuarioRepository.buscarPorId(any()) }
    }
}