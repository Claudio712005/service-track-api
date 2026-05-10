package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.dto.request.OrdemServicoReqDTO
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

class CriarOrdemServicoServiceTest {

    private val repository = mockk<OrdemServicoRepositoryPort>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val jwt = mockk<JwtPort>()

    private val service = CriarOrdemServicoService(repository, usuarioRepository, jwt)

    private val clienteId = UsuarioId.gerar()
    private val mecanicoId = UsuarioId.gerar()
    private val veiculoId = VeiculoId.gerar()

    private val requisicao = OrdemServicoReqDTO(
        motivo = "Troca de óleo e revisão geral",
        clienteId = clienteId,
        mecanicoId = mecanicoId,
        veiculoId = veiculoId,
        observaco = null
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
    fun `deve criar ordem de servico quando mecanico faz a requisicao`() {
        val mecanico = buildMecanico(mecanicoId)
        val cliente = buildCliente(clienteId)

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { repository.contarOsAbertaPorIdVeiculoEIdCliente(clienteId, veiculoId) } returns 0L
        every { repository.salvar(any()) } answers { firstArg() }

        val result = service.criarOrdemServico(requisicao)

        assertNotNull(result.id)
        assertEquals(clienteId.valor, result.clienteId)
        assertEquals(mecanicoId.valor, result.mecanicoId)
        assertEquals("Troca de óleo e revisão geral", result.motivo)
        verify(exactly = 1) { repository.salvar(any()) }
    }

    @Test
    fun `deve criar ordem de servico quando cliente faz a requisicao para si mesmo`() {
        val cliente = buildCliente(clienteId)
        val mecanico = buildMecanico(mecanicoId)

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { repository.contarOsAbertaPorIdVeiculoEIdCliente(clienteId, veiculoId) } returns 0L
        every { repository.salvar(any()) } answers { firstArg() }

        val result = service.criarOrdemServico(requisicao)

        assertNotNull(result.id)
        assertEquals(clienteId.valor, result.clienteId)
        verify(exactly = 1) { repository.salvar(any()) }
    }

    @Test
    fun `deve criar ordem de servico com observacao quando informada`() {
        val mecanico = buildMecanico(mecanicoId)
        val cliente = buildCliente(clienteId)
        val reqComObservacao = requisicao.copy(observaco = "Veículo apresenta barulho ao frear")

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { repository.contarOsAbertaPorIdVeiculoEIdCliente(clienteId, veiculoId) } returns 0L
        every { repository.salvar(any()) } answers { firstArg() }

        val result = service.criarOrdemServico(reqComObservacao)

        assertNotNull(result.id)
        assertEquals("Veículo apresenta barulho ao frear", result.observacao)
        verify(exactly = 1) { repository.salvar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando usuario logado nao encontrado`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.criarOrdemServico(requisicao)
        }

        verify(exactly = 0) { repository.salvar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando cliente tenta criar OS para outro cliente`() {
        val outroClienteId = UsuarioId.gerar()
        val clienteLogado = buildCliente(outroClienteId)

        every { jwt.getUsuarioId() } returns outroClienteId
        every { usuarioRepository.buscarPorId(outroClienteId) } returns clienteLogado

        assertThrows<OperacaoNegadaException> {
            service.criarOrdemServico(requisicao)
        }

        verify(exactly = 0) { repository.salvar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando cliente informado nao encontrado`() {
        val mecanico = buildMecanico(mecanicoId)

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(clienteId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.criarOrdemServico(requisicao)
        }

        verify(exactly = 0) { repository.salvar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando usuario informado como cliente e mecanico`() {
        val mecanico = buildMecanico(mecanicoId)
        val clienteEhMecanico = buildMecanico(clienteId)

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(clienteId) } returns clienteEhMecanico

        assertThrows<OperacaoNegadaException> {
            service.criarOrdemServico(requisicao)
        }

        verify(exactly = 0) { repository.salvar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando mecanico informado nao encontrado`() {
        val mecanico = buildMecanico(mecanicoId)
        val cliente = buildCliente(clienteId)

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returnsMany listOf(mecanico, null)
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente

        assertThrows<EntidadeNaoEncontradaException> {
            service.criarOrdemServico(requisicao)
        }

        verify(exactly = 0) { repository.salvar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando usuario informado como mecanico e cliente`() {
        val mecanicoEhCliente = buildCliente(mecanicoId)
        val cliente = buildCliente(clienteId)
        val mecanicoLogado = buildMecanico(UsuarioId.gerar())
        val mecanicoLogadoId = mecanicoLogado.id

        every { jwt.getUsuarioId() } returns mecanicoLogadoId
        every { usuarioRepository.buscarPorId(mecanicoLogadoId) } returns mecanicoLogado
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanicoEhCliente

        assertThrows<OperacaoNegadaException> {
            service.criarOrdemServico(requisicao)
        }

        verify(exactly = 0) { repository.salvar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando ja existe OS aberta para o veiculo e cliente`() {
        val mecanico = buildMecanico(mecanicoId)
        val cliente = buildCliente(clienteId)

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { repository.contarOsAbertaPorIdVeiculoEIdCliente(clienteId, veiculoId) } returns 1L

        assertThrows<OperacaoNegadaException> {
            service.criarOrdemServico(requisicao)
        }

        verify(exactly = 0) { repository.salvar(any()) }
    }
}