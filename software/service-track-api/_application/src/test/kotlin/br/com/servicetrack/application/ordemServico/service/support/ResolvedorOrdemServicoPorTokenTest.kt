package br.com.servicetrack.application.ordemServico.service.support

import br.com.servicetrack.application.exception.LinkDecisaoInvalidoException
import br.com.servicetrack.application.ordemServico.ports.out.DecisaoOrcamentoTokenClaims
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.ports.out.TokenDecisaoOrcamentoPort
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class ResolvedorOrdemServicoPorTokenTest {

    private val tokenPort = mockk<TokenDecisaoOrcamentoPort>()
    private val osRepository = mockk<OrdemServicoRepositoryPort>()
    private val resolvedor = ResolvedorOrdemServicoPorToken(tokenPort, osRepository)

    private val clienteId = UsuarioId.gerar()
    private val osId = OrdemServicoId.gerar()

    private fun buildOs(clienteId: UsuarioId = this.clienteId): OrdemServico = OrdemServico.reconstituir(
        id = osId,
        motivo = "Revisão",
        observacao = "",
        clienteId = clienteId,
        mecanicoId = UsuarioId.gerar(),
        veiculoId = VeiculoId.gerar(),
        dataCriacao = LocalDateTime.now(),
        dataAtualizacao = LocalDateTime.now(),
        status = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO),
        prazoConclusao = null,
        orcamento = null,
        insumos = mutableListOf(),
        itensServico = mutableListOf(),
    )

    @Test
    fun `deve resolver a OS quando token valido e dono confere`() {
        every { tokenPort.validar("tok") } returns DecisaoOrcamentoTokenClaims(osId, clienteId)
        every { osRepository.buscarPorId(osId) } returns buildOs()

        val os = resolvedor.resolver("tok")

        assertEquals(osId, os.id)
    }

    @Test
    fun `deve lancar quando token invalido`() {
        every { tokenPort.validar("tok") } returns null

        assertThrows<LinkDecisaoInvalidoException> { resolvedor.resolver("tok") }
    }

    @Test
    fun `deve lancar quando OS nao encontrada`() {
        every { tokenPort.validar("tok") } returns DecisaoOrcamentoTokenClaims(osId, clienteId)
        every { osRepository.buscarPorId(osId) } returns null

        assertThrows<LinkDecisaoInvalidoException> { resolvedor.resolver("tok") }
    }

    @Test
    fun `deve lancar quando token nao pertence ao dono da OS`() {
        every { tokenPort.validar("tok") } returns DecisaoOrcamentoTokenClaims(osId, UsuarioId.gerar())
        every { osRepository.buscarPorId(osId) } returns buildOs(clienteId = clienteId)

        assertThrows<LinkDecisaoInvalidoException> { resolvedor.resolver("tok") }
    }
}
