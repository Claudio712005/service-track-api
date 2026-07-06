package br.com.servicetrack.application.notificacao.event

import br.com.servicetrack.application.notificacao.dto.EnfileirarNotificacaoCommand
import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.application.ordemServico.ports.out.AprovacaoOrcamentoLinkPort
import br.com.servicetrack.application.ordemServico.ports.out.LinksDecisaoOrcamento
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class OrdemServicoStatusAlteradoListenerTest {

    private val enfileirar = mockk<EnfileirarNotificacaoUseCase>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val aprovacaoLink = mockk<AprovacaoOrcamentoLinkPort>()
    private val listener = OrdemServicoStatusAlteradoListener(enfileirar, usuarioRepository, aprovacaoLink)

    private val clienteId = UsuarioId.gerar()
    private val osId = OrdemServicoId.gerar()

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
}
