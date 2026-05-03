package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.dto.request.ConcluirItemServicoReqDTO
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.ordemServico.ItemOrdemServico
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.ItemOrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime

class ConcluirItemServicoServiceTest {

    private val osRepository = mockk<OrdemServicoRepositoryPort>()
    private val jwt = mockk<JwtPort>()

    private val service = ConcluirItemServicoService(osRepository, jwt)

    private val mecanicoId = UsuarioId.gerar()
    private val clienteId = UsuarioId.gerar()

    private fun buildItem(osId: OrdemServicoId): ItemOrdemServico {
        val agora = LocalDateTime.now()
        return ItemOrdemServico.reconstituir(
            id = ItemOrdemServicoId.gerar(),
            servicoId = ServicoId.gerar(),
            ordemServicoId = osId,
            valor = ValorMonetario(BigDecimal("120.00")),
            feito = false,
            mecanicoResponsavelId = mecanicoId,
            dataRealizacao = null,
            observacao = null,
            dataCriacao = agora,
            dataAtualizacao = agora,
        )
    }

    private fun buildOs(
        status: StatusOrdemServicoEnum = StatusOrdemServicoEnum.EM_EXECUCAO,
        mecanicoId: UsuarioId = this.mecanicoId,
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
    fun `deve concluir item de servico quando mecanico vinculado faz a requisicao`() {
        val osId = OrdemServicoId.gerar()
        val item = buildItem(osId)
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
            itensServico = mutableListOf(item),
        )
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns os
        every { osRepository.atualizar(any()) } answers { firstArg() }

        val result = service.concluirItemServico(
            ordemServicoId = os.id.valor,
            itemServicoId = item.id.valor,
            req = ConcluirItemServicoReqDTO("Serviço realizado com sucesso"),
        )

        assertNotNull(result)
        verify(exactly = 1) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando OS nao encontrada`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { osRepository.buscarPorId(any()) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.concluirItemServico(
                ordemServicoId = OrdemServicoId.gerar().valor,
                itemServicoId = ItemOrdemServicoId.gerar().valor,
                req = ConcluirItemServicoReqDTO("Observação"),
            )
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando solicitante nao e o mecanico vinculado`() {
        val os = buildOs()
        val outroMecanicoId = UsuarioId.gerar()
        every { jwt.getUsuarioId() } returns outroMecanicoId
        every { osRepository.buscarPorId(any()) } returns os

        assertThrows<OperacaoNegadaException> {
            service.concluirItemServico(
                ordemServicoId = os.id.valor,
                itemServicoId = ItemOrdemServicoId.gerar().valor,
                req = ConcluirItemServicoReqDTO("Observação"),
            )
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }
}
