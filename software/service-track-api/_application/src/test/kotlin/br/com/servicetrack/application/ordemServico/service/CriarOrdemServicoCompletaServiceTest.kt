package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.ordemServico.dto.request.CriarOrdemServicoCompletaReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.ItemInsumoReqDTO
import br.com.servicetrack.application.ordemServico.dto.request.ItemServicoReqDTO
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.service.support.AbridorOrdemServico
import br.com.servicetrack.application.ordemServico.service.support.AssociadorItensOrdemServico
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import io.mockk.verify
import jakarta.enterprise.event.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class CriarOrdemServicoCompletaServiceTest {

    private val osRepository = mockk<OrdemServicoRepositoryPort>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val servicoRepository = mockk<ServicoRepositoryPort>()
    private val insumoRepository = mockk<InsumoRepositoryPort>()
    private val jwt = mockk<JwtPort>()
    private val statusAlteradoEvent = mockk<Event<OrdemServicoStatusAlteradoEvent>>()

    private val service = CriarOrdemServicoCompletaService(
        osRepository,
        usuarioRepository,
        jwt,
        AbridorOrdemServico(usuarioRepository, osRepository),
        AssociadorItensOrdemServico(servicoRepository, insumoRepository),
        statusAlteradoEvent,
    )

    private val clienteId = UsuarioId.gerar()
    private val mecanicoId = UsuarioId.gerar()
    private val veiculoId = VeiculoId.gerar()
    private val servicoId = ServicoId.gerar()
    private val insumoId = InsumoId.gerar()

    private fun req() = CriarOrdemServicoCompletaReqDTO(
        motivo = "Revisão completa",
        clienteId = clienteId,
        veiculoId = veiculoId,
        observacao = "Cliente relatou barulho",
        servicos = listOf(ItemServicoReqDTO(servicoId.valor, null)),
        insumos = listOf(ItemInsumoReqDTO(insumoId.valor, 2)),
    )

    private fun buildCliente(id: UsuarioId = clienteId): Usuario = Usuario.reconstituir(
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
        dataAtualizacao = LocalDateTime.now(),
    )

    private fun buildMecanico(id: UsuarioId = mecanicoId): Usuario = Usuario.reconstituir(
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
        dataAtualizacao = LocalDateTime.now(),
    )

    private fun buildServico(): Servico = Servico.reconstituir(
        id = servicoId,
        nomeServico = "Troca de Óleo",
        descricaoServico = "Substituição do óleo do motor",
        valorReferencia = ValorMonetario(BigDecimal("150.00")),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
    )

    private fun buildInsumo(qtd: Int = 10): Insumo = Insumo.reconstituir(
        id = insumoId,
        nome = "Filtro de Óleo",
        descricao = "Filtro motor 1.0",
        custo = ValorMonetario(BigDecimal("25.00")),
        estoqueMinimo = 2,
        qtdEstoque = qtd,
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
    )

    private fun stubAberturaValida() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns buildMecanico()
        every { usuarioRepository.buscarPorId(clienteId) } returns buildCliente()
        every { osRepository.contarOsAbertaPorIdVeiculoEIdCliente(clienteId, veiculoId) } returns 0L
        every { servicoRepository.buscarPorId(servicoId) } returns buildServico()
        every { insumoRepository.buscarPorId(insumoId) } returns buildInsumo()
        every { osRepository.salvar(any()) } answers { firstArg() }
        every { statusAlteradoEvent.fire(any()) } just Runs
    }

    @Test
    fun `deve abrir OS completa em diagnostico com itens quando mecanico solicita`() {
        stubAberturaValida()

        val result = service.criarOrdemServicoCompleta(req())

        assertNotNull(result.id)
        assertEquals(StatusOrdemServicoEnum.EM_DIAGNOSTICO, result.status)
        assertEquals(clienteId.valor, result.clienteId)
        assertEquals(mecanicoId.valor, result.mecanicoId)
        assertEquals(1, result.itensServico.size)
        verify(exactly = 1) { osRepository.salvar(any()) }
    }

    @Test
    fun `deve disparar evento de mudanca de status para EM_DIAGNOSTICO`() {
        stubAberturaValida()
        val eventoSlot = slot<OrdemServicoStatusAlteradoEvent>()
        every { statusAlteradoEvent.fire(capture(eventoSlot)) } just Runs

        service.criarOrdemServicoCompleta(req())

        assertEquals(StatusOrdemServicoEnum.EM_DIAGNOSTICO, eventoSlot.captured.novoStatus)
        assertEquals(clienteId, eventoSlot.captured.clienteId)
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando solicitante nao encontrado`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.criarOrdemServicoCompleta(req())
        }

        verify(exactly = 0) { osRepository.salvar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando solicitante nao e mecanico`() {
        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns buildCliente()

        assertThrows<OperacaoNegadaException> {
            service.criarOrdemServicoCompleta(req())
        }

        verify(exactly = 0) { osRepository.salvar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando cliente informado e mecanico`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns buildMecanico()
        every { usuarioRepository.buscarPorId(clienteId) } returns buildMecanico(clienteId)

        assertThrows<OperacaoNegadaException> {
            service.criarOrdemServicoCompleta(req())
        }

        verify(exactly = 0) { osRepository.salvar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando ja existe OS aberta para veiculo e cliente`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns buildMecanico()
        every { usuarioRepository.buscarPorId(clienteId) } returns buildCliente()
        every { osRepository.contarOsAbertaPorIdVeiculoEIdCliente(clienteId, veiculoId) } returns 1L

        assertThrows<OperacaoNegadaException> {
            service.criarOrdemServicoCompleta(req())
        }

        verify(exactly = 0) { osRepository.salvar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando lista de servicos vazia`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns buildMecanico()
        every { usuarioRepository.buscarPorId(clienteId) } returns buildCliente()
        every { osRepository.contarOsAbertaPorIdVeiculoEIdCliente(clienteId, veiculoId) } returns 0L

        assertThrows<DomainException> {
            service.criarOrdemServicoCompleta(req().copy(servicos = emptyList()))
        }

        verify(exactly = 0) { osRepository.salvar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando estoque insuficiente`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns buildMecanico()
        every { usuarioRepository.buscarPorId(clienteId) } returns buildCliente()
        every { osRepository.contarOsAbertaPorIdVeiculoEIdCliente(clienteId, veiculoId) } returns 0L
        every { servicoRepository.buscarPorId(servicoId) } returns buildServico()
        every { insumoRepository.buscarPorId(insumoId) } returns buildInsumo(qtd = 1)

        assertThrows<DomainException> {
            service.criarOrdemServicoCompleta(req())
        }

        verify(exactly = 0) { osRepository.salvar(any()) }
    }
}
