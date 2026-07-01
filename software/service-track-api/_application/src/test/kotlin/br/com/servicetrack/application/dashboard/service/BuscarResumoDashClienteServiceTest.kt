package br.com.servicetrack.application.dashboard.service

import br.com.servicetrack.application.dashboard.dto.query.OrdemServicoDashboardQueryDTO
import br.com.servicetrack.application.dashboard.dto.query.VeiculoDashboardQueryDTO
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class BuscarResumoDashClienteServiceTest {

    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val veiculoRepository = mockk<VeiculoRepositoryPort>()
    private val ordemServicoRepository = mockk<OrdemServicoRepositoryPort>()
    private val jwt = mockk<JwtPort>()

    private val service = BuscarResumoDashClienteService(
        usuarioRepository,
        veiculoRepository,
        ordemServicoRepository,
        jwt,
    )

    private val clienteId = UsuarioId.gerar()

    private fun buildCliente(id: UsuarioId = clienteId) = Usuario.reconstituir(
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

    private fun buildVeiculoQuery(id: String = VeiculoId.gerar().valor) = VeiculoDashboardQueryDTO(
        id = id,
        placa = "ABC1D23",
        marca = "Honda",
        modelo = "Civic",
        ano = 2021,
        imagemUrl = null,
        codigoFipe = null,
        ativo = true,
        dataCriacao = LocalDateTime.now().minusDays(30),
    )

    private fun buildOrdemAtivaQuery(veiculoId: String) = OrdemServicoDashboardQueryDTO(
        id = "os-ativa-1",
        motivo = "Barulho ao frear",
        status = StatusOrdemServicoEnum.EM_DIAGNOSTICO,
        veiculoId = veiculoId,
        veiculoPlaca = "ABC1D23",
        veiculoModelo = "Civic",
        mecanicoId = "mecanico-1",
        mecanicoNome = "Mecânico Teste",
        dataCriacao = LocalDateTime.now().minusDays(2),
        dataAtualizacao = LocalDateTime.now().minusDays(1),
        prazoConclusao = LocalDateTime.now().plusDays(3),
        valorOrcado = null,
    )

    private fun buildOrdemRecenteQuery(veiculoId: String) = OrdemServicoDashboardQueryDTO(
        id = "os-recente-1",
        motivo = "Troca de óleo",
        status = StatusOrdemServicoEnum.FINALIZADA,
        veiculoId = veiculoId,
        veiculoPlaca = "ABC1D23",
        veiculoModelo = "Civic",
        mecanicoId = "mecanico-1",
        mecanicoNome = "Mecânico Teste",
        dataCriacao = LocalDateTime.now().minusDays(10),
        dataAtualizacao = LocalDateTime.now().minusDays(8),
        prazoConclusao = null,
        valorOrcado = BigDecimal("350.00"),
    )

    private val statusAtivos = listOf(
        StatusOrdemServicoEnum.RECEBIDA,
        StatusOrdemServicoEnum.EM_DIAGNOSTICO,
        StatusOrdemServicoEnum.AGUARDANDO_APROVACAO,
        StatusOrdemServicoEnum.EM_EXECUCAO,
        StatusOrdemServicoEnum.ENTREGUE,
    )

    @Test
    fun `deve retornar dashboard completo quando cliente acessa seu proprio dashboard`() {
        val cliente = buildCliente()
        val veiculo = buildVeiculoQuery()
        val ordemAtiva = buildOrdemAtivaQuery(veiculo.id)
        val ordemRecente = buildOrdemRecenteQuery(veiculo.id)

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { veiculoRepository.listarDashboardPorProprietario(clienteId) } returns listOf(veiculo)
        every { ordemServicoRepository.listarAtivasDashboardPorCliente(clienteId, 10) } returns listOf(ordemAtiva)
        every { ordemServicoRepository.listarRecentesDashboardPorCliente(clienteId, 10) } returns listOf(ordemRecente)
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, statusAtivos) } returns 1L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.FINALIZADA)) } returns 1L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.CANCELADA)) } returns 0L
        every { ordemServicoRepository.contarTotalPorVeiculo(VeiculoId(veiculo.id), clienteId) } returns 2L
        every { ordemServicoRepository.somarGastoPorVeiculo(VeiculoId(veiculo.id), clienteId) } returns BigDecimal("350.00")

        val result = service.buscarResumo(clienteId.valor)

        assertNotNull(result)
        assertEquals(clienteId.valor, result.usuarioId)
        assertEquals("Cliente Teste", result.usuarioNome)
        assertEquals(1, result.resumo.ordensAtivas)
        assertEquals(1, result.resumo.ordensConcluidas)
        assertEquals(0, result.resumo.ordensCanceladas)
        assertEquals(2, result.resumo.totalOrdens)
        assertEquals(1, result.resumo.veiculosCadastrados)
        assertEquals(1, result.ordensAtivas.size)
        assertEquals(1, result.ordensRecentes.size)
        assertEquals(1, result.veiculos.size)
    }

    @Test
    fun `deve retornar ordens ativas com dias em andamento calculados`() {
        val cliente = buildCliente()
        val veiculo = buildVeiculoQuery()
        val ordemAtiva = buildOrdemAtivaQuery(veiculo.id)

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { veiculoRepository.listarDashboardPorProprietario(clienteId) } returns listOf(veiculo)
        every { ordemServicoRepository.listarAtivasDashboardPorCliente(clienteId, 10) } returns listOf(ordemAtiva)
        every { ordemServicoRepository.listarRecentesDashboardPorCliente(clienteId, 10) } returns emptyList()
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, statusAtivos) } returns 1L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.FINALIZADA)) } returns 0L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.CANCELADA)) } returns 0L
        every { ordemServicoRepository.contarTotalPorVeiculo(VeiculoId(veiculo.id), clienteId) } returns 1L
        every { ordemServicoRepository.somarGastoPorVeiculo(VeiculoId(veiculo.id), clienteId) } returns BigDecimal.ZERO

        val result = service.buscarResumo(clienteId.valor)

        assertEquals(1, result.ordensAtivas.size)
        val osAtiva = result.ordensAtivas.first()
        assertEquals("EM_DIAGNOSTICO", osAtiva.status)
        assertEquals(2, osAtiva.diasEmAndamento)
        assertEquals("Mecânico Teste", osAtiva.mecanicoNome)
    }

    @Test
    fun `deve calcular data de conclusao para ordens recentes finalizadas`() {
        val cliente = buildCliente()
        val veiculo = buildVeiculoQuery()
        val ordemRecente = buildOrdemRecenteQuery(veiculo.id)

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { veiculoRepository.listarDashboardPorProprietario(clienteId) } returns listOf(veiculo)
        every { ordemServicoRepository.listarAtivasDashboardPorCliente(clienteId, 10) } returns emptyList()
        every { ordemServicoRepository.listarRecentesDashboardPorCliente(clienteId, 10) } returns listOf(ordemRecente)
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, statusAtivos) } returns 0L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.FINALIZADA)) } returns 1L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.CANCELADA)) } returns 0L
        every { ordemServicoRepository.contarTotalPorVeiculo(VeiculoId(veiculo.id), clienteId) } returns 1L
        every { ordemServicoRepository.somarGastoPorVeiculo(VeiculoId(veiculo.id), clienteId) } returns BigDecimal("350.00")

        val result = service.buscarResumo(clienteId.valor)

        assertEquals(1, result.ordensRecentes.size)
        val osRecente = result.ordensRecentes.first()
        assertNotNull(osRecente.dataConclusao)
        assertNotNull(osRecente.diasParaConclusao)
        assertEquals(BigDecimal("350.00"), osRecente.valorTotal)
    }

    @Test
    fun `deve retornar listas vazias quando cliente nao tem ordens nem veiculos`() {
        val cliente = buildCliente()

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { veiculoRepository.listarDashboardPorProprietario(clienteId) } returns emptyList()
        every { ordemServicoRepository.listarAtivasDashboardPorCliente(clienteId, 10) } returns emptyList()
        every { ordemServicoRepository.listarRecentesDashboardPorCliente(clienteId, 10) } returns emptyList()
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, statusAtivos) } returns 0L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.FINALIZADA)) } returns 0L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.CANCELADA)) } returns 0L

        val result = service.buscarResumo(clienteId.valor)

        assertEquals(0, result.resumo.totalOrdens)
        assertEquals(0, result.resumo.veiculosCadastrados)
        assertEquals(emptyList<Any>(), result.ordensAtivas)
        assertEquals(emptyList<Any>(), result.ordensRecentes)
        assertEquals(emptyList<Any>(), result.veiculos)
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando cliente tenta acessar dashboard de outro cliente`() {
        val outroClienteId = UsuarioId.gerar()

        every { jwt.getUsuarioId() } returns outroClienteId

        assertThrows<OperacaoNegadaException> {
            service.buscarResumo(clienteId.valor)
        }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando cliente nao encontrado`() {
        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.buscarResumo(clienteId.valor)
        }
    }

    @Test
    fun `deve calcular total_gasto como zero quando cliente nao tem gastos`() {
        val cliente = buildCliente()
        val veiculo = buildVeiculoQuery()

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { veiculoRepository.listarDashboardPorProprietario(clienteId) } returns listOf(veiculo)
        every { ordemServicoRepository.listarAtivasDashboardPorCliente(clienteId, 10) } returns emptyList()
        every { ordemServicoRepository.listarRecentesDashboardPorCliente(clienteId, 10) } returns emptyList()
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, statusAtivos) } returns 0L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.FINALIZADA)) } returns 0L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.CANCELADA)) } returns 0L
        every { ordemServicoRepository.contarTotalPorVeiculo(VeiculoId(veiculo.id), clienteId) } returns 0L
        every { ordemServicoRepository.somarGastoPorVeiculo(VeiculoId(veiculo.id), clienteId) } returns BigDecimal.ZERO

        val result = service.buscarResumo(clienteId.valor)

        assertEquals(1, result.veiculos.size)
        assertEquals(BigDecimal.ZERO, result.veiculos.first().totalGasto)
        assertEquals(0, result.veiculos.first().totalOrdens)
    }

    @Test
    fun `deve nao calcular data de conclusao para ordens ativas recentes`() {
        val cliente = buildCliente()
        val veiculo = buildVeiculoQuery()
        val ordemAtiva = buildOrdemRecenteQuery(veiculo.id).copy(
            status = StatusOrdemServicoEnum.EM_DIAGNOSTICO,
        )

        every { jwt.getUsuarioId() } returns clienteId
        every { usuarioRepository.buscarPorId(clienteId) } returns cliente
        every { veiculoRepository.listarDashboardPorProprietario(clienteId) } returns listOf(veiculo)
        every { ordemServicoRepository.listarAtivasDashboardPorCliente(clienteId, 10) } returns emptyList()
        every { ordemServicoRepository.listarRecentesDashboardPorCliente(clienteId, 10) } returns listOf(ordemAtiva)
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, statusAtivos) } returns 1L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.FINALIZADA)) } returns 0L
        every { ordemServicoRepository.contarPorClienteEStatus(clienteId, listOf(StatusOrdemServicoEnum.CANCELADA)) } returns 0L
        every { ordemServicoRepository.contarTotalPorVeiculo(VeiculoId(veiculo.id), clienteId) } returns 1L
        every { ordemServicoRepository.somarGastoPorVeiculo(VeiculoId(veiculo.id), clienteId) } returns BigDecimal.ZERO

        val result = service.buscarResumo(clienteId.valor)

        val osRecente = result.ordensRecentes.first()
        assertEquals(null, osRecente.dataConclusao)
        assertEquals(null, osRecente.diasParaConclusao)
    }
}
