package br.com.servicetrack.domain.servico

import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ServicoTest {

    private val valorRef = ValorMonetario(BigDecimal("80.00"))

    private fun buildServico(
        nome: String = "Troca de Óleo",
        descricao: String = "Troca do óleo do motor e filtro",
        valorReferencia: ValorMonetario? = null,
    ): Servico = Servico.gerar(
        nomeServico = nome,
        descricaoServico = descricao,
        valorReferencia = valorReferencia,
    )

    @Test
    fun `deve gerar serviço com campos corretos`() {
        val servico = buildServico()

        assertNotNull(servico.id)
        assertEquals("Troca de Óleo", servico.nomeServico)
        assertEquals("Troca do óleo do motor e filtro", servico.descricaoServico)
        assertNull(servico.valorReferencia)
        assertNotNull(servico.dataCriacao)
    }

    @Test
    fun `deve gerar serviço com valor de referência`() {
        val servico = buildServico(valorReferencia = valorRef)
        assertEquals(BigDecimal("80.00"), servico.valorReferencia!!.valor)
    }


    @Test
    fun `deve gerar serviço com valor de referência padrão`() {
        val servico = Servico.gerar(
            nomeServico = "Alinhamento",
            descricaoServico = "Alinhamento computadorizado",
        )

        assertNull(servico.valorReferencia)
    }

    @Test
    fun `deve gerar ids únicos a cada criação`() {
        val a = buildServico()
        val b = buildServico()
        assertTrue(a.id != b.id)
    }

    @Test
    fun `deve lançar exceção ao criar serviço com nome vazio`() {
        val ex = assertThrows<IllegalArgumentException> {
            buildServico(nome = "")
        }
        assertEquals("Nome do serviço não pode ser vazio", ex.message)
    }

    @Test
    fun `deve lançar exceção ao criar serviço com nome em branco`() {
        val ex = assertThrows<IllegalArgumentException> {
            buildServico(nome = "   ")
        }
        assertEquals("Nome do serviço não pode ser vazio", ex.message)
    }

    @Test
    fun `deve lançar exceção ao criar serviço com descrição vazia`() {
        val ex = assertThrows<IllegalArgumentException> {
            buildServico(descricao = "")
        }
        assertEquals("Descrição do serviço não pode ser vazia", ex.message)
    }

    @Test
    fun `deve lançar exceção ao criar serviço com descrição em branco`() {
        val ex = assertThrows<IllegalArgumentException> {
            buildServico(descricao = "   ")
        }
        assertEquals("Descrição do serviço não pode ser vazia", ex.message)
    }

    @Test
    fun `deve atualizar valor de referência`() {
        val servico = buildServico()
        val novoValor = ValorMonetario(BigDecimal("120.00"))

        servico.atualizarValorReferencia(novoValor)

        assertEquals(BigDecimal("120.00"), servico.valorReferencia!!.valor)
    }

    @Test
    fun `deve sobrescrever valor de referência existente`() {
        val servico = buildServico(valorReferencia = valorRef)
        val novo = ValorMonetario(BigDecimal("150.00"))

        servico.atualizarValorReferencia(novo)

        assertEquals(BigDecimal("150.00"), servico.valorReferencia!!.valor)
    }

    @Test
    fun `deve lançar exceção ao atualizar descrição com valor vazio`() {
        val servico = buildServico()
        val ex = assertThrows<DomainException> {
            servico.atualizarDescricao("")
        }
        assertEquals("Descrição do serviço não pode ser vazia", ex.message)
    }

    @Test
    fun `deve lançar exceção ao atualizar descrição com valor em branco`() {
        val servico = buildServico()
        val ex = assertThrows<DomainException> {
            servico.atualizarDescricao("   ")
        }
        assertEquals("Descrição do serviço não pode ser vazia", ex.message)
    }

    @Test
    fun `deve reconstituir serviço a partir dos dados de persistência`() {
        val id = br.com.servicetrack.domain.servico.vo.ServicoId.gerar()
        val agora = java.time.LocalDateTime.now()

        val servico = Servico.reconstituir(
            id = id,
            nomeServico = "Alinhamento",
            descricaoServico = "Alinhamento computadorizado 4 rodas",
            valorReferencia = valorRef,
            dataCriacao = agora,
            dataAtualizacao = agora,
        )

        assertEquals(id, servico.id)
        assertEquals("Alinhamento", servico.nomeServico)
        assertEquals(BigDecimal("80.00"), servico.valorReferencia!!.valor)
    }

    @Test
    fun `deve reconstituir serviço sem valor de referência`() {
        val agora = java.time.LocalDateTime.now()

        val servico = Servico.reconstituir(
            id = br.com.servicetrack.domain.servico.vo.ServicoId.gerar(),
            nomeServico = "Diagnóstico Elétrico",
            descricaoServico = "Scanner OBD2",
            valorReferencia = null,
            dataCriacao = agora,
            dataAtualizacao = agora,
        )

        assertNull(servico.valorReferencia)
    }
}
