package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.exception.VeiculoJaExisteException
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.dto.request.CadastrarVeiculoReqDTO
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
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

class CadastrarVeiculoServiceTest {

    private val veiculoRepository = mockk<VeiculoRepositoryPort>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val jwt = mockk<JwtPort>()

    private val service = CadastrarVeiculoService(veiculoRepository, usuarioRepository, jwt)

    private val proprietarioId = UsuarioId.gerar()
    private val tokenUsuarioId = proprietarioId

    private val requisicao = CadastrarVeiculoReqDTO(
        placa = "ABC1D23",
        marca = "Honda",
        modelo = "Civic",
        ano = 2020,
        proprietarioId = proprietarioId.valor
    )

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

    @Test
    fun `deve cadastrar veiculo com sucesso quando proprietario e usuario token sao o mesmo`() {
        val proprietario = buildCliente(proprietarioId)

        every { veiculoRepository.existeVeiculoPorPlaca(requisicao.placa) } returns false
        every { usuarioRepository.buscarPorId(proprietarioId) } returns proprietario
        every { jwt.getUsuarioId() } returns tokenUsuarioId
        every { veiculoRepository.salvar(any()) } returns Unit

        val response = service.cadastrarVeiculo(requisicao)

        assertNotNull(response)
        assertEquals("ABC1D23", response.placa)
        assertEquals("Honda", response.marca)
        assertEquals("Civic", response.modelo)
        assertEquals(2020, response.ano)
        verify(exactly = 1) { veiculoRepository.salvar(any()) }
    }

    @Test
    fun `deve cadastrar veiculo quando usuario token e mecanico e proprietario e diferente`() {
        val mecanicoId = UsuarioId.gerar()
        val mecanico = buildMecanico(mecanicoId)
        val proprietario = buildCliente(proprietarioId)

        every { veiculoRepository.existeVeiculoPorPlaca(requisicao.placa) } returns false
        every { usuarioRepository.buscarPorId(proprietarioId) } returns proprietario
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { veiculoRepository.salvar(any()) } returns Unit

        val response = service.cadastrarVeiculo(requisicao)

        assertNotNull(response)
        verify(exactly = 1) { veiculoRepository.salvar(any()) }
    }

    @Test
    fun `deve lançar excecao quando placa ja existe`() {
        every { veiculoRepository.existeVeiculoPorPlaca(requisicao.placa) } returns true

        assertThrows<VeiculoJaExisteException> {
            service.cadastrarVeiculo(requisicao)
        }

        verify(exactly = 0) { veiculoRepository.salvar(any()) }
    }

    @Test
    fun `deve lançar excecao quando proprietario nao encontrado`() {
        every { veiculoRepository.existeVeiculoPorPlaca(requisicao.placa) } returns false
        every { usuarioRepository.buscarPorId(proprietarioId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.cadastrarVeiculo(requisicao)
        }

        verify(exactly = 0) { veiculoRepository.salvar(any()) }
    }

    @Test
    fun `deve lançar excecao quando usuario token nao encontrado`() {
        val outroId = UsuarioId.gerar()
        val proprietario = buildCliente(proprietarioId)

        every { veiculoRepository.existeVeiculoPorPlaca(requisicao.placa) } returns false
        every { usuarioRepository.buscarPorId(proprietarioId) } returns proprietario
        every { jwt.getUsuarioId() } returns outroId
        every { usuarioRepository.buscarPorId(outroId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.cadastrarVeiculo(requisicao)
        }

        verify(exactly = 0) { veiculoRepository.salvar(any()) }
    }

    @Test
    fun `deve lançar OperacaoNegadaException quando cliente tenta cadastrar para outro cliente`() {
        val outroClienteId = UsuarioId.gerar()
        val outroCliente = buildCliente(outroClienteId)
        val proprietario = buildCliente(proprietarioId)

        every { veiculoRepository.existeVeiculoPorPlaca(requisicao.placa) } returns false
        every { usuarioRepository.buscarPorId(proprietarioId) } returns proprietario
        every { jwt.getUsuarioId() } returns outroClienteId
        every { usuarioRepository.buscarPorId(outroClienteId) } returns outroCliente

        assertThrows<OperacaoNegadaException> {
            service.cadastrarVeiculo(requisicao)
        }

        verify(exactly = 0) { veiculoRepository.salvar(any()) }
    }
}
