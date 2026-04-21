package br.com.servicetrack.application.mecanico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.mecanico.dto.request.AtualizarMecanicoReqDTO
import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class AtualizarMecanicoServiceTest {

    private val repository = mockk<MecanicoRepositoryPort>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val jwt = mockk<JwtPort>()

    private val service = AtualizarMecanicoService(repository, usuarioRepository, jwt)

    private val mecanicoSeniorId = UsuarioId.gerar()
    private val mecanicoAlvoId = UsuarioId.gerar()

    private fun buildMecanico(id: UsuarioId, nivel: NivelMecanicoEnum, valorHora: BigDecimal): Mecanico =
        Mecanico.criar(id, ValorHora(valorHora), NivelMecanico.criar(nivel))

    private fun buildUsuario(id: UsuarioId): Usuario = Usuario.reconstituir(
        id = id,
        nome = "Mecânico Teste",
        email = Email("mecanico@oficina.com"),
        senhaHash = Senha.deHash("\$2a\$10\$hashFake"),
        dataNascimento = LocalDate.of(1985, 3, 20),
        telefone = Telefone("11999999999"),
        cpf = Cpf("14716682072"),
        ativo = true,
        roles = setOf(Role.MECANICO),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now()
    )

    private val requisicao = AtualizarMecanicoReqDTO(
        nivel = NivelMecanicoEnum.PLENO,
        valorHora = BigDecimal("90.00")
    )

    @Test
    fun `deve atualizar mecanico com sucesso quando mecanico Senior faz a requisicao`() {
        val mecanicoSenior = buildMecanico(mecanicoSeniorId, NivelMecanicoEnum.SENIOR, BigDecimal("150.00"))
        val mecanicoAlvo = buildMecanico(mecanicoAlvoId, NivelMecanicoEnum.JUNIOR, BigDecimal("50.00"))
        val mecanicoAtualizado = buildMecanico(mecanicoAlvoId, NivelMecanicoEnum.PLENO, BigDecimal("90.00"))
        val usuarioAlvo = buildUsuario(mecanicoAlvoId)

        every { jwt.getUsuarioId() } returns mecanicoSeniorId
        every { repository.buscarPorId(mecanicoAlvoId.valor) } returns mecanicoAlvo
        every { repository.buscarPorId(mecanicoSeniorId.valor) } returns mecanicoSenior
        every { repository.atualizar(any()) } returns mecanicoAtualizado
        every { usuarioRepository.buscarPorId(mecanicoAlvoId) } returns usuarioAlvo

        val resultado = service.atualizarMecanico(mecanicoAlvoId.valor, requisicao)

        assertEquals(NivelMecanicoEnum.PLENO.name, resultado.nivel)
        assertEquals(BigDecimal("90.00"), resultado.valorHora)
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando mecanico tenta atualizar a si mesmo`() {
        every { jwt.getUsuarioId() } returns mecanicoSeniorId

        assertThrows<OperacaoNegadaException> {
            service.atualizarMecanico(mecanicoSeniorId.valor, requisicao)
        }

        verify(exactly = 0) { repository.buscarPorId(any()) }
        verify(exactly = 0) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando mecanico alvo nao encontrado`() {
        every { jwt.getUsuarioId() } returns mecanicoSeniorId
        every { repository.buscarPorId(mecanicoAlvoId.valor) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.atualizarMecanico(mecanicoAlvoId.valor, requisicao)
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando mecanico logado nao encontrado`() {
        val mecanicoAlvo = buildMecanico(mecanicoAlvoId, NivelMecanicoEnum.JUNIOR, BigDecimal("50.00"))

        every { jwt.getUsuarioId() } returns mecanicoSeniorId
        every { repository.buscarPorId(mecanicoAlvoId.valor) } returns mecanicoAlvo
        every { repository.buscarPorId(mecanicoSeniorId.valor) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.atualizarMecanico(mecanicoAlvoId.valor, requisicao)
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando mecanico logado nao e Senior`() {
        val mecanicoJunior = buildMecanico(mecanicoSeniorId, NivelMecanicoEnum.JUNIOR, BigDecimal("50.00"))
        val mecanicoAlvo = buildMecanico(mecanicoAlvoId, NivelMecanicoEnum.JUNIOR, BigDecimal("50.00"))

        every { jwt.getUsuarioId() } returns mecanicoSeniorId
        every { repository.buscarPorId(mecanicoAlvoId.valor) } returns mecanicoAlvo
        every { repository.buscarPorId(mecanicoSeniorId.valor) } returns mecanicoJunior

        assertThrows<OperacaoNegadaException> {
            service.atualizarMecanico(mecanicoAlvoId.valor, requisicao)
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando atualizar retorna null`() {
        val mecanicoSenior = buildMecanico(mecanicoSeniorId, NivelMecanicoEnum.SENIOR, BigDecimal("150.00"))
        val mecanicoAlvo = buildMecanico(mecanicoAlvoId, NivelMecanicoEnum.JUNIOR, BigDecimal("50.00"))

        every { jwt.getUsuarioId() } returns mecanicoSeniorId
        every { repository.buscarPorId(mecanicoAlvoId.valor) } returns mecanicoAlvo
        every { repository.buscarPorId(mecanicoSeniorId.valor) } returns mecanicoSenior
        every { repository.atualizar(any()) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.atualizarMecanico(mecanicoAlvoId.valor, requisicao)
        }
    }
}
