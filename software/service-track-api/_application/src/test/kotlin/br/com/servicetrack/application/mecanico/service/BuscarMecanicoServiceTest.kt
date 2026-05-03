package br.com.servicetrack.application.mecanico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO
import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.mecanico.Mecanico
import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import br.com.servicetrack.domain.mecanico.vo.NivelMecanico
import br.com.servicetrack.domain.mecanico.vo.ValorHora
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class BuscarMecanicoServiceTest {

    private lateinit var mecanicoRepository: MecanicoRepositoryPort
    private lateinit var usuarioRepository: UsuarioRepositoryPort
    private lateinit var service: BuscarMecanicoService

    @BeforeEach
    fun setup() {
        mecanicoRepository = mockk()
        usuarioRepository = mockk()
        service = BuscarMecanicoService(mecanicoRepository, usuarioRepository)
    }

    @Test
    fun `deve retornar mecanico quando encontrado`() {
        val mecanicoId = UsuarioId.gerar()

        val mecanico = Mecanico.criar(
            usuarioId = mecanicoId,
            valorHora = ValorHora(BigDecimal("100.00")),
            nivel = NivelMecanico.criar(NivelMecanicoEnum.PLENO)
        )

        val usuario = Usuario.criarMecanico(
            nome = "João Silva",
            email = Email("email@email.com"),
            senha = Senha.criar("@Senha123"),
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = Telefone("11999999999"),
            cpf = Cpf("72732437018")
        )

        every { mecanicoRepository.buscarPorId(any()) } returns mecanico
        every { usuarioRepository.buscarPorId(any()) } returns usuario

        val resultado = service.buscarMecanico(mecanicoId.valor)

        assertEquals(usuario.id.valor, resultado.usuarioId)

        verify { mecanicoRepository.buscarPorId(any()) }
        verify { usuarioRepository.buscarPorId(any()) }
    }

    @Test
    fun `deve lancar excecao quando mecanico nao encontrado`() {
        val mecanicoId = "1"

        every { mecanicoRepository.buscarPorId(mecanicoId) } returns null

        val exception = assertThrows(EntidadeNaoEncontradaException::class.java) {
            service.buscarMecanico(mecanicoId)
        }

        assertTrue(exception.message!!.contains("Mecanico"))

        verify { mecanicoRepository.buscarPorId(mecanicoId) }
        verify(exactly = 0) { usuarioRepository.buscarPorId(any()) }
    }

    @Test
    fun `deve lancar excecao quando usuario nao encontrado`() {
        val mecanicoId = "1"
        val usuarioId = UsuarioId("10")

        val mecanico = Mecanico.criar(
            usuarioId = usuarioId,
            valorHora = ValorHora(BigDecimal("100.00")),
            nivel = NivelMecanico.criar(NivelMecanicoEnum.PLENO)
        )

        every { mecanicoRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(usuarioId) } returns null

        val exception = assertThrows(EntidadeNaoEncontradaException::class.java) {
            service.buscarMecanico(mecanicoId)
        }

        assertTrue(exception.message!!.contains("Usuario"))

        verify { mecanicoRepository.buscarPorId(mecanicoId) }
        verify { usuarioRepository.buscarPorId(usuarioId) }
    }
}