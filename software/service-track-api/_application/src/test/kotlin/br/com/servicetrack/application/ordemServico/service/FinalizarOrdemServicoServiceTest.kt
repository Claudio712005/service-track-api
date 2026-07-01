package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.ordemServico.ItemOrdemServico
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.ItemOrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
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
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import jakarta.enterprise.event.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class FinalizarOrdemServicoServiceTest {

    private val osRepository = mockk<OrdemServicoRepositoryPort>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val jwt = mockk<JwtPort>()
    private val statusEvent = mockk<Event<OrdemServicoStatusAlteradoEvent>>()

    private val service = FinalizarOrdemServicoService(osRepository, usuarioRepository, jwt, statusEvent)

    private val mecanicoId = UsuarioId.gerar()
    private val clienteId = UsuarioId.gerar()

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

    private fun buildItemFeito(osId: OrdemServicoId): ItemOrdemServico {
        val agora = LocalDateTime.now()
        return ItemOrdemServico.reconstituir(
            id = ItemOrdemServicoId.gerar(),
            servicoId = ServicoId.gerar(),
            ordemServicoId = osId,
            valor = ValorMonetario(BigDecimal("120.00")),
            feito = true,
            mecanicoResponsavelId = mecanicoId,
            dataRealizacao = agora,
            observacao = "Serviço concluído",
            dataCriacao = agora,
            dataAtualizacao = agora,
        )
    }

    private fun buildItemPendente(osId: OrdemServicoId): ItemOrdemServico {
        val agora = LocalDateTime.now()
        return ItemOrdemServico.reconstituir(
            id = ItemOrdemServicoId.gerar(),
            servicoId = ServicoId.gerar(),
            ordemServicoId = osId,
            valor = ValorMonetario(BigDecimal("120.00")),
            feito = false,
            mecanicoResponsavelId = null,
            dataRealizacao = null,
            observacao = null,
            dataCriacao = agora,
            dataAtualizacao = agora,
        )
    }

    private fun buildOs(
        status: StatusOrdemServicoEnum = StatusOrdemServicoEnum.EM_EXECUCAO,
        itens: List<ItemOrdemServico> = emptyList(),
    ): OrdemServico {
        val osId = OrdemServicoId.gerar()
        return OrdemServico.reconstituir(
            id = osId,
            motivo = "Revisão geral",
            observacao = "",
            clienteId = clienteId,
            mecanicoId = mecanicoId,
            veiculoId = VeiculoId.gerar(),
            dataCriacao = LocalDateTime.now(),
            dataAtualizacao = LocalDateTime.now(),
            status = StatusOrdemServico.deEnum(status),
            prazoConclusao = null,
            orcamento = null,
            insumos = mutableListOf(),
            itensServico = itens.toMutableList(),
        )
    }

    @Test
    fun `deve finalizar OS quando todos os servicos estao concluidos`() {
        val osId = OrdemServicoId.gerar()
        val itemFeito = buildItemFeito(osId)
        val os = OrdemServico.reconstituir(
            id = osId,
            motivo = "Revisão geral",
            observacao = "",
            clienteId = clienteId,
            mecanicoId = mecanicoId,
            veiculoId = VeiculoId.gerar(),
            dataCriacao = LocalDateTime.now(),
            dataAtualizacao = LocalDateTime.now(),
            status = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.EM_EXECUCAO),
            prazoConclusao = null,
            orcamento = null,
            insumos = mutableListOf(),
            itensServico = mutableListOf(itemFeito),
        )
        val mecanico = buildMecanico()
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { osRepository.buscarPorId(any()) } returns os
        every { osRepository.atualizar(any()) } answers { firstArg() }
        justRun { statusEvent.fire(any()) }

        val result = service.finalizarOrdemServico(os.id.valor)

        assertEquals(StatusOrdemServicoEnum.FINALIZADA, result.status)
        verify(exactly = 1) { osRepository.atualizar(any()) }
        verify(exactly = 1) {
            statusEvent.fire(match { it.novoStatus == StatusOrdemServicoEnum.FINALIZADA })
        }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando solicitante nao encontrado`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.finalizarOrdemServico(OrdemServicoId.gerar().valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando solicitante e um cliente`() {
        val cliente = buildCliente(mecanicoId)
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns cliente

        assertThrows<OperacaoNegadaException> {
            service.finalizarOrdemServico(OrdemServicoId.gerar().valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando OS nao encontrada`() {
        val mecanico = buildMecanico()
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { osRepository.buscarPorId(any()) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.finalizarOrdemServico(OrdemServicoId.gerar().valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando existem servicos pendentes`() {
        val osId = OrdemServicoId.gerar()
        val itemPendente = buildItemPendente(osId)
        val os = OrdemServico.reconstituir(
            id = osId,
            motivo = "Revisão geral",
            observacao = "",
            clienteId = clienteId,
            mecanicoId = mecanicoId,
            veiculoId = VeiculoId.gerar(),
            dataCriacao = LocalDateTime.now(),
            dataAtualizacao = LocalDateTime.now(),
            status = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.EM_EXECUCAO),
            prazoConclusao = null,
            orcamento = null,
            insumos = mutableListOf(),
            itensServico = mutableListOf(itemPendente),
        )
        val mecanico = buildMecanico()
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { osRepository.buscarPorId(any()) } returns os

        assertThrows<DomainException> {
            service.finalizarOrdemServico(os.id.valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }
}
