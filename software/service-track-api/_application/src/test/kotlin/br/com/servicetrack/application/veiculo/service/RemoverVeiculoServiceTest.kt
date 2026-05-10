package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.shared.enums.IndicativoSimNao
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.Placa
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

class RemoverVeiculoServiceTest {

    private val jwt = mockk<JwtPort>()
    private val veiculoRepository = mockk<VeiculoRepositoryPort>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()

    private val service = RemoverVeiculoService(jwt, veiculoRepository, usuarioRepository)

    private val proprietarioId = UsuarioId.gerar()
    private val veiculoId = VeiculoId.gerar()

    private fun buildCliente(id: UsuarioId = UsuarioId.gerar()): Usuario = Usuario.reconstituir(
        id = id,
        nome = "Cliente Teste",
        email = Email("cliente@email.com"),
        senhaHash = Senha.deHash("\$2a\$10\$hashFake"),
        dataNascimento = LocalDate.of(1990, 1, 1),
        telefone = Telefone("11999999999"),
        cpf = Cpf("14716682072"),
        ativo = true,
        roles = setOf(Role.CLIENTE),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now()
    )

    private fun buildMecanico(id: UsuarioId = UsuarioId.gerar()): Usuario = Usuario.reconstituir(
        id = id,
        nome = "Mecânico Teste",
        email = Email("mecanico@oficina.com"),
        senhaHash = Senha.deHash("\$2a\$10\$hashFake"),
        dataNascimento = LocalDate.of(1985, 3, 20),
        telefone = Telefone("11988887777"),
        cpf = Cpf("52998224725"),
        ativo = true,
        roles = setOf(Role.MECANICO),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now()
    )

    private fun buildVeiculo(proprietario: UsuarioId = proprietarioId): Veiculo =
        Veiculo.reconstituir(
            id = veiculoId,
            marca = "Honda",
            placa = Placa("ABC1D23"),
            ano = 2020,
            proprietarioId = proprietario,
            modelo = "Civic",
            dataCriacao = LocalDateTime.now(),
            dataAtualizacao = LocalDateTime.now(),
            ativo = IndicativoSimNao.S
        )

    @Test
    fun `deve remover veiculo quando usuario e o proprietario`() {
        val proprietario = buildCliente(proprietarioId)
        val veiculo = buildVeiculo(proprietarioId)

        every { jwt.getUsuarioId() } returns proprietarioId
        every { usuarioRepository.buscarPorId(proprietarioId) } returns proprietario
        every { veiculoRepository.buscarPorId(veiculoId) } returns veiculo
        every { veiculoRepository.desativar(veiculoId) } returns Unit

        service.removerVeiculo(veiculoId)

        verify(exactly = 1) { veiculoRepository.desativar(veiculoId) }
    }

    @Test
    fun `deve remover veiculo quando usuario e mecanico mesmo sem ser proprietario`() {
        val mecanicoId = UsuarioId.gerar()
        val mecanico = buildMecanico(mecanicoId)
        val veiculo = buildVeiculo(proprietarioId)

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { veiculoRepository.buscarPorId(veiculoId) } returns veiculo
        every { veiculoRepository.desativar(veiculoId) } returns Unit

        service.removerVeiculo(veiculoId)

        verify(exactly = 1) { veiculoRepository.desativar(veiculoId) }
    }

    @Test
    fun `deve lançar OperacaoNegadaException quando usuario do token nao encontrado`() {
        val outroId = UsuarioId.gerar()

        every { jwt.getUsuarioId() } returns outroId
        every { usuarioRepository.buscarPorId(outroId) } returns null

        assertThrows<OperacaoNegadaException> {
            service.removerVeiculo(veiculoId)
        }

        verify(exactly = 0) { veiculoRepository.desativar(any()) }
    }

    @Test
    fun `deve lançar EntidadeNaoEncontradaException quando veiculo nao encontrado`() {
        val cliente = buildCliente(proprietarioId)

        every { jwt.getUsuarioId() } returns proprietarioId
        every { usuarioRepository.buscarPorId(proprietarioId) } returns cliente
        every { veiculoRepository.buscarPorId(veiculoId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.removerVeiculo(veiculoId)
        }

        verify(exactly = 0) { veiculoRepository.desativar(any()) }
    }

    @Test
    fun `deve lançar OperacaoNegadaException quando cliente nao e proprietario do veiculo`() {
        val outroClienteId = UsuarioId.gerar()
        val outroCliente = buildCliente(outroClienteId)
        val veiculo = buildVeiculo(proprietarioId)

        every { jwt.getUsuarioId() } returns outroClienteId
        every { usuarioRepository.buscarPorId(outroClienteId) } returns outroCliente
        every { veiculoRepository.buscarPorId(veiculoId) } returns veiculo

        assertThrows<OperacaoNegadaException> {
            service.removerVeiculo(veiculoId)
        }

        verify(exactly = 0) { veiculoRepository.desativar(any()) }
    }
}
