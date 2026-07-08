package br.com.servicetrack.application.notificacao.event

import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.application.notificacao.dto.EnfileirarNotificacaoCommand
import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.application.ordemServico.ports.out.AprovacaoOrcamentoLinkPort
import br.com.servicetrack.application.ordemServico.ports.out.LinksDecisaoOrcamento
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.orcamento.Orcamento
import br.com.servicetrack.domain.ordemServico.ItemOrdemServico
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.ItemOrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.PrazoConclusao
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.DadosVeiculo
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.Placa
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class OrdemServicoStatusAlteradoListenerTest {

    private val enfileirar = mockk<EnfileirarNotificacaoUseCase>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val aprovacaoLink = mockk<AprovacaoOrcamentoLinkPort>()
    private val ordemServicoRepository = mockk<OrdemServicoRepositoryPort>()
    private val veiculoRepository = mockk<VeiculoRepositoryPort>()
    private val servicoRepository = mockk<ServicoRepositoryPort>()
    private val insumoRepository = mockk<InsumoRepositoryPort>()
    private val listener = OrdemServicoStatusAlteradoListener(
        enfileirar,
        usuarioRepository,
        aprovacaoLink,
        ordemServicoRepository,
        veiculoRepository,
        servicoRepository,
        insumoRepository,
    )

    private val clienteId = UsuarioId.gerar()
    private val mecanicoId = UsuarioId.gerar()
    private val osId = OrdemServicoId.gerar()
    private val veiculoId = VeiculoId.gerar()
    private val servicoId = ServicoId.gerar()
    private val insumoId = InsumoId.gerar()

    private fun buildCliente(nome: String = "Cláudio"): Usuario = Usuario.reconstituir(
        id = clienteId,
        nome = nome,
        email = Email("cliente@x.com"),
        senhaHash = Senha.deHash("\$2a\$10\$hashFake"),
        dataNascimento = LocalDate.of(1990, 1, 1),
        telefone = Telefone("11999999999"),
        cpf = Cpf("14716682072"),
        ativo = true,
        roles = setOf(Role.CLIENTE),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
    )

    private fun buildMecanico(): Usuario = Usuario.reconstituir(
        id = mecanicoId,
        nome = "Zé Mecânico",
        email = Email("mecanico@x.com"),
        senhaHash = Senha.deHash("\$2a\$10\$hashFake"),
        dataNascimento = LocalDate.of(1985, 5, 5),
        telefone = Telefone("11988888888"),
        cpf = Cpf("39053344705"),
        ativo = true,
        roles = setOf(Role.MECANICO),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
    )

    private fun buildItem(): ItemOrdemServico = ItemOrdemServico.reconstituir(
        id = ItemOrdemServicoId.gerar(),
        servicoId = servicoId,
        ordemServicoId = osId,
        valor = ValorMonetario(BigDecimal("150.00")),
        feito = false,
        mecanicoResponsavelId = null,
        dataRealizacao = null,
        observacao = "",
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
    )

    private fun buildOs(comOrcamento: Boolean, comPrazo: Boolean): OrdemServico = OrdemServico.reconstituir(
        id = osId,
        motivo = "Barulho no motor",
        observacao = "",
        clienteId = clienteId,
        mecanicoId = mecanicoId,
        veiculoId = veiculoId,
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
        status = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO),
        prazoConclusao = if (comPrazo) PrazoConclusao(LocalDateTime.now().plusDays(3)) else null,
        orcamento = if (comOrcamento) {
            Orcamento.gerar(ValorMonetario(BigDecimal("200.00")), ValorMonetario(BigDecimal("50.00")))
        } else {
            null
        },
        insumos = mutableListOf(insumoId),
        itensServico = mutableListOf(buildItem()),
    )

    private fun buildVeiculo(): Veiculo {
        val veiculo = mockk<Veiculo>()
        every { veiculo.obterDados() } returns DadosVeiculo(
            id = veiculoId,
            proprietarioId = clienteId,
            placa = Placa("ABC1D23"),
            modelo = "Uno",
            marca = "Fiat",
            ano = 2020,
        )
        return veiculo
    }

    @Test
    fun `deve enfileirar notificacao com variaveis do template`() {
        every { usuarioRepository.buscarPorId(clienteId) } returns buildCliente("Cláudio")
        val capturado = slot<EnfileirarNotificacaoCommand>()
        every { enfileirar.executar(capture(capturado)) } returns NotificacaoId.gerar()

        listener.aoAlterarStatus(
            OrdemServicoStatusAlteradoEvent(
                ordemServicoId = osId,
                clienteId = clienteId,
                novoStatus = StatusOrdemServicoEnum.EM_DIAGNOSTICO,
            )
        )

        val cmd = capturado.captured
        assertEquals(clienteId, cmd.destinatario)
        assertEquals(TipoNotificacao.EMAIL, cmd.tipoNotificacao)
        assertEquals(TipoConteudoNotificacao.MUDANCA_STATUS_OS, cmd.tipoConteudoNotificacao)
        assertEquals(osId.valor, cmd.variaveis["os"])
        assertEquals(StatusOrdemServicoEnum.EM_DIAGNOSTICO.descricao, cmd.variaveis["novoStatus"])
        assertEquals("Cláudio", cmd.variaveis["nomeCliente"])
    }

    @Test
    fun `deve enfileirar tambem o e-mail de aprovacao quando status AGUARDANDO_APROVACAO`() {
        every { usuarioRepository.buscarPorId(clienteId) } returns buildCliente("Cláudio")
        every { aprovacaoLink.gerarLinks(osId, clienteId) } returns LinksDecisaoOrcamento(
            aprovarUrl = "http://api/ordem-servico/orcamento/aprovacao?token=abc",
            reprovarUrl = "http://api/ordem-servico/orcamento/reprovacao?token=abc",
        )
        every { ordemServicoRepository.buscarPorId(osId) } returns null
        val comandos = mutableListOf<EnfileirarNotificacaoCommand>()
        every { enfileirar.executar(capture(comandos)) } returns NotificacaoId.gerar()

        listener.aoAlterarStatus(
            OrdemServicoStatusAlteradoEvent(
                ordemServicoId = osId,
                clienteId = clienteId,
                novoStatus = StatusOrdemServicoEnum.AGUARDANDO_APROVACAO,
            )
        )

        assertEquals(2, comandos.size)
        val aprovacao = comandos.first {
            it.tipoConteudoNotificacao == TipoConteudoNotificacao.SOLICITACAO_APROVACAO_ORCAMENTO_OS
        }
        assertEquals(clienteId, aprovacao.destinatario)
        assertEquals("http://api/ordem-servico/orcamento/aprovacao?token=abc", aprovacao.variaveis["aprovarUrl"])
        assertEquals("http://api/ordem-servico/orcamento/reprovacao?token=abc", aprovacao.variaveis["reprovarUrl"])
        verify(exactly = 1) { aprovacaoLink.gerarLinks(osId, clienteId) }
    }

    @Test
    fun `nao deve enfileirar quando cliente nao encontrado`() {
        every { usuarioRepository.buscarPorId(clienteId) } returns null

        listener.aoAlterarStatus(
            OrdemServicoStatusAlteradoEvent(
                ordemServicoId = osId,
                clienteId = clienteId,
                novoStatus = StatusOrdemServicoEnum.FINALIZADA,
            )
        )

        verify(exactly = 0) { enfileirar.executar(any()) }
    }

    @Test
    fun `deve montar aprovacao com dados completos da OS`() {
        every { usuarioRepository.buscarPorId(clienteId) } returns buildCliente("Cláudio")
        every { usuarioRepository.buscarPorId(mecanicoId) } returns buildMecanico()
        every { aprovacaoLink.gerarLinks(osId, clienteId) } returns LinksDecisaoOrcamento(
            aprovarUrl = "http://api/aprovacao?token=abc",
            reprovarUrl = "http://api/reprovacao?token=abc",
        )
        every { ordemServicoRepository.buscarPorId(osId) } returns buildOs(comOrcamento = true, comPrazo = true)
        every { veiculoRepository.buscarPorId(veiculoId) } returns buildVeiculo()

        val servico = mockk<Servico>()
        every { servico.nomeServico } returns "Troca de Óleo"
        every { servicoRepository.buscarPorId(servicoId) } returns servico

        val insumo = mockk<Insumo>()
        every { insumo.nome } returns "Filtro"
        every { insumo.calcularCusto(1) } returns ValorMonetario(BigDecimal("30.00"))
        every { insumoRepository.buscarPorId(insumoId) } returns insumo

        val comandos = mutableListOf<EnfileirarNotificacaoCommand>()
        every { enfileirar.executar(capture(comandos)) } returns NotificacaoId.gerar()

        listener.aoAlterarStatus(
            OrdemServicoStatusAlteradoEvent(
                ordemServicoId = osId,
                clienteId = clienteId,
                novoStatus = StatusOrdemServicoEnum.AGUARDANDO_APROVACAO,
            )
        )

        val aprovacao = comandos.first {
            it.tipoConteudoNotificacao == TipoConteudoNotificacao.SOLICITACAO_APROVACAO_ORCAMENTO_OS
        }
        assertEquals("Fiat Uno (2020)", aprovacao.variaveis["veiculo"])
        assertEquals("ABC1D23", aprovacao.variaveis["placa"])
        assertEquals("Zé Mecânico", aprovacao.variaveis["mecanico"])
        assertEquals("Barulho no motor", aprovacao.variaveis["motivo"])
        assertEquals("R$ 250,00", aprovacao.variaveis["valorTotal"])
        assertTrue(aprovacao.variaveis["servicosHtml"]!!.contains("Troca de Óleo"))
        assertTrue(aprovacao.variaveis["servicosTexto"]!!.contains("Troca de Óleo"))
        assertTrue(aprovacao.variaveis["insumosHtml"]!!.contains("Filtro"))
        assertTrue(aprovacao.variaveis["insumosTexto"]!!.contains("Filtro"))
    }

    @Test
    fun `deve usar fallbacks quando dados relacionados ausentes`() {
        every { usuarioRepository.buscarPorId(clienteId) } returns buildCliente("Cláudio")
        every { usuarioRepository.buscarPorId(mecanicoId) } returns null
        every { aprovacaoLink.gerarLinks(osId, clienteId) } returns LinksDecisaoOrcamento(
            aprovarUrl = "http://api/aprovacao?token=abc",
            reprovarUrl = "http://api/reprovacao?token=abc",
        )
        every { ordemServicoRepository.buscarPorId(osId) } returns buildOs(comOrcamento = false, comPrazo = false)
        every { veiculoRepository.buscarPorId(veiculoId) } returns null
        every { servicoRepository.buscarPorId(servicoId) } returns null
        every { insumoRepository.buscarPorId(insumoId) } returns null

        val comandos = mutableListOf<EnfileirarNotificacaoCommand>()
        every { enfileirar.executar(capture(comandos)) } returns NotificacaoId.gerar()

        listener.aoAlterarStatus(
            OrdemServicoStatusAlteradoEvent(
                ordemServicoId = osId,
                clienteId = clienteId,
                novoStatus = StatusOrdemServicoEnum.AGUARDANDO_APROVACAO,
            )
        )

        val aprovacao = comandos.first {
            it.tipoConteudoNotificacao == TipoConteudoNotificacao.SOLICITACAO_APROVACAO_ORCAMENTO_OS
        }
        assertEquals("Não informado", aprovacao.variaveis["veiculo"])
        assertEquals("-", aprovacao.variaveis["placa"])
        assertEquals("-", aprovacao.variaveis["mecanico"])
        assertEquals("R$ 0,00", aprovacao.variaveis["valorTotal"])
        assertEquals("A combinar", aprovacao.variaveis["prazoConclusao"])
        assertTrue(aprovacao.variaveis["servicosTexto"]!!.contains(servicoId.valor))
        assertTrue(aprovacao.variaveis["insumosTexto"]!!.contains(insumoId.valor))
    }
}
