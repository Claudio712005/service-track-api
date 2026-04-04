package br.com.servicetrack.domain.ordemServico

import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OrdemServicoTest {

    private fun buildOS(): OrdemServico {
        return OrdemServico.abrir(
            motivo = "Troca de óleo e filtros",
            clienteId = UsuarioId.gerar(),
            mecanicoId = UsuarioId.gerar(),
            veiculoId = VeiculoId.gerar()
        )
    }

    private val custoMaoDeObra = ValorMonetario(BigDecimal("200.00"))
    private val custoInsumos = ValorMonetario(BigDecimal("100.00"))

    @Test
    fun `deve abrir OS com status RECEBIDA`() {
        val os = buildOS()
        assertEquals(StatusOrdemServicoEnum.RECEBIDA, os.obterStatus())
    }

    @Test
    fun `deve abrir OS sem orçamento`() {
        val os = buildOS()
        assertNull(os.obterOrcamento())
    }

    @Test
    fun `deve abrir OS sem insumos`() {
        val os = buildOS()
        assertTrue(os.listarInsumos().isEmpty())
    }

    @Test
    fun `deve lançar exceção ao abrir OS com motivo vazio`() {
        val exception = assertThrows<IllegalArgumentException> {
            OrdemServico.abrir(
                motivo = "",
                clienteId = UsuarioId.gerar(),
                mecanicoId = UsuarioId.gerar(),
                veiculoId = VeiculoId.gerar()
            )
        }
        assertEquals("Motivo da OS não pode ser vazio", exception.message)
    }

    @Test
    fun `deve transitar RECEBIDA para EM_DIAGNOSTICO`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        assertEquals(StatusOrdemServicoEnum.EM_DIAGNOSTICO, os.obterStatus())
    }

    @Test
    fun `deve transitar para AGUARDANDO_APROVACAO ao gerar orçamento`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        os.gerarOrcamento(custoMaoDeObra, custoInsumos)
        assertEquals(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO, os.obterStatus())
    }

    @Test
    fun `deve transitar para EM_EXECUCAO ao aprovar orçamento`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        os.gerarOrcamento(custoMaoDeObra, custoInsumos)
        os.aprovarOrcamento()
        assertEquals(StatusOrdemServicoEnum.EM_EXECUCAO, os.obterStatus())
    }

    @Test
    fun `deve transitar para FINALIZADA`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        os.gerarOrcamento(custoMaoDeObra, custoInsumos)
        os.aprovarOrcamento()
        os.finalizar()
        assertEquals(StatusOrdemServicoEnum.FINALIZADA, os.obterStatus())
    }

    @Test
    fun `deve transitar para ENTREGUE`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        os.gerarOrcamento(custoMaoDeObra, custoInsumos)
        os.aprovarOrcamento()
        os.finalizar()
        os.entregar()
        assertEquals(StatusOrdemServicoEnum.ENTREGUE, os.obterStatus())
    }

    @Test
    fun `deve cancelar OS em qualquer etapa válida`() {
        val os = buildOS()
        os.cancelar("Solicitação do cliente")
        assertEquals(StatusOrdemServicoEnum.CANCELADA, os.obterStatus())
    }

    @Test
    fun `deve transitar para CANCELADA ao reprovar orçamento`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        os.gerarOrcamento(custoMaoDeObra, custoInsumos)
        os.reprovarOrcamento("Valor muito alto")
        assertEquals(StatusOrdemServicoEnum.CANCELADA, os.obterStatus())
    }

    @Test
    fun `deve lançar exceção para transição de status inválida`() {
        val os = buildOS()
        assertThrows<IllegalStateException> {
            os.finalizar()
        }
    }

    @Test
    fun `deve adicionar insumo durante diagnóstico`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        val insumoId = InsumoId.gerar()

        os.adicionarInsumo(insumoId)

        assertTrue(os.listarInsumos().contains(insumoId))
    }

    @Test
    fun `deve adicionar múltiplos insumos durante diagnóstico`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        val id1 = InsumoId.gerar()
        val id2 = InsumoId.gerar()

        os.adicionarInsumo(id1)
        os.adicionarInsumo(id2)

        assertEquals(2, os.listarInsumos().size)
    }

    @Test
    fun `deve lançar exceção ao adicionar insumo fora do diagnóstico`() {
        val os = buildOS()
        val exception = assertThrows<IllegalStateException> {
            os.adicionarInsumo(InsumoId.gerar())
        }
        assertTrue(exception.message!!.contains("diagnóstico"))
    }

    @Test
    fun `deve remover insumo durante diagnóstico`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        val insumoId = InsumoId.gerar()
        os.adicionarInsumo(insumoId)

        os.removerInsumo(insumoId)

        assertFalse(os.listarInsumos().contains(insumoId))
    }

    @Test
    fun `deve lançar exceção ao remover insumo inexistente`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        assertThrows<IllegalStateException> {
            os.removerInsumo(InsumoId.gerar())
        }
    }

    @Test
    fun `listarInsumos deve retornar cópia defensiva`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        os.adicionarInsumo(InsumoId.gerar())

        val snapshot = os.listarInsumos()
        os.adicionarInsumo(InsumoId.gerar())

        assertEquals(1, snapshot.size)
        assertEquals(2, os.listarInsumos().size)
    }

    @Test
    fun `deve gerar orçamento com valores corretos`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        os.gerarOrcamento(custoMaoDeObra, custoInsumos)

        val orcamento = os.obterOrcamento()
        assertNotNull(orcamento)
        assertEquals(BigDecimal("200.00"), orcamento.custoMaoDeObra.valor)
        assertEquals(BigDecimal("100.00"), orcamento.custoInsumos.valor)
        assertEquals(BigDecimal("300.00"), orcamento.valorTotal.valor)
    }

    @Test
    fun `deve lançar exceção ao gerar orçamento fora do diagnóstico`() {
        val os = buildOS()
        val exception = assertThrows<IllegalStateException> {
            os.gerarOrcamento(custoMaoDeObra, custoInsumos)
        }
        assertTrue(exception.message!!.contains("diagnóstico"))
    }

    @Test
    fun `deve marcar orçamento como aprovado após aprovação`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        os.gerarOrcamento(custoMaoDeObra, custoInsumos)
        os.aprovarOrcamento()

        assertTrue(os.obterOrcamento()!!.estaAprovado())
    }

    @Test
    fun `deve lançar exceção ao aprovar OS sem orçamento`() {
        val os = buildOS()
        os.iniciarDiagnostico()
        assertThrows<DomainException> {
            os.aprovarOrcamento()
        }
    }

    @Test
    fun `deve definir prazo de conclusão válido`() {
        val os = buildOS()
        val prazo = LocalDateTime.now().plusDays(3)
        os.definirPrazoConclusao(prazo)
        assertNotNull(os)
    }

    @Test
    fun `deve lançar exceção ao definir prazo no passado`() {
        val os = buildOS()
        val exception = assertThrows<DomainException> {
            os.definirPrazoConclusao(LocalDateTime.now().minusDays(1))
        }
        assertTrue(exception.message!!.contains("passado"))
    }

    @Test
    fun `deve lançar exceção ao redefinir prazo já existente`() {
        val os = buildOS()
        os.definirPrazoConclusao(LocalDateTime.now().plusDays(3))
        assertThrows<IllegalStateException> {
            os.definirPrazoConclusao(LocalDateTime.now().plusDays(5))
        }
    }

    @Test
    fun `deve reassinar mecânico diferente`() {
        val os = buildOS()
        val novoMecanicoId = UsuarioId.gerar()
        os.reassinarMecanico(novoMecanicoId)
        assertEquals(novoMecanicoId, os.obterMecanicoId())
    }

    @Test
    fun `deve lançar exceção ao reassinar mesmo mecânico`() {
        val mecanicoId = UsuarioId.gerar()
        val os = OrdemServico.abrir(
            motivo = "Revisão geral",
            clienteId = UsuarioId.gerar(),
            mecanicoId = mecanicoId,
            veiculoId = VeiculoId.gerar()
        )
        val exception = assertThrows<IllegalStateException> {
            os.reassinarMecanico(mecanicoId)
        }
        assertEquals("O mecânico já está atribuído a esta OS", exception.message)
    }
}
