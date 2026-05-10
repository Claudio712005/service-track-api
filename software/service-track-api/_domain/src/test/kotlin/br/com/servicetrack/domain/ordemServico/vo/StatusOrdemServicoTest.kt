package br.com.servicetrack.domain.ordemServico.vo

import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class StatusOrdemServicoTest {

    @Test
    fun `deve criar status a partir de enum`() {
        val status = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.RECEBIDA)
        assertEquals(StatusOrdemServicoEnum.RECEBIDA, status.valor)
    }

    @Test
    fun `deve criar status a partir da ordem correta`() {
        assertEquals(StatusOrdemServicoEnum.RECEBIDA, StatusOrdemServico.de(1).valor)
        assertEquals(StatusOrdemServicoEnum.EM_DIAGNOSTICO, StatusOrdemServico.de(2).valor)
        assertEquals(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO, StatusOrdemServico.de(3).valor)
        assertEquals(StatusOrdemServicoEnum.EM_EXECUCAO, StatusOrdemServico.de(4).valor)
        assertEquals(StatusOrdemServicoEnum.FINALIZADA, StatusOrdemServico.de(5).valor)
        assertEquals(StatusOrdemServicoEnum.ENTREGUE, StatusOrdemServico.de(6).valor)
    }

    @Test
    fun `deve lançar exceção para ordem inválida`() {
        val exception = assertThrows<IllegalArgumentException> {
            StatusOrdemServico.de(99)
        }
        assertEquals("Status com ordem 99 não encontrado", exception.message)
    }

    @Test
    fun `deve transitar de RECEBIDA para EM_DIAGNOSTICO`() {
        val novo = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.RECEBIDA)
            .transitarPara(StatusOrdemServicoEnum.EM_DIAGNOSTICO)
        assertEquals(StatusOrdemServicoEnum.EM_DIAGNOSTICO, novo.valor)
    }

    @Test
    fun `deve transitar de EM_DIAGNOSTICO para AGUARDANDO_APROVACAO`() {
        val novo = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.EM_DIAGNOSTICO)
            .transitarPara(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO)
        assertEquals(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO, novo.valor)
    }

    @Test
    fun `deve transitar de AGUARDANDO_APROVACAO para EM_EXECUCAO`() {
        val novo = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO)
            .transitarPara(StatusOrdemServicoEnum.EM_EXECUCAO)
        assertEquals(StatusOrdemServicoEnum.EM_EXECUCAO, novo.valor)
    }

    @Test
    fun `deve transitar de EM_EXECUCAO para FINALIZADA`() {
        val novo = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.EM_EXECUCAO)
            .transitarPara(StatusOrdemServicoEnum.FINALIZADA)
        assertEquals(StatusOrdemServicoEnum.FINALIZADA, novo.valor)
    }

    @Test
    fun `deve transitar de FINALIZADA para ENTREGUE`() {
        val novo = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.FINALIZADA)
            .transitarPara(StatusOrdemServicoEnum.ENTREGUE)
        assertEquals(StatusOrdemServicoEnum.ENTREGUE, novo.valor)
    }

    @Test
    fun `deve permitir cancelamento de RECEBIDA`() {
        val novo = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.RECEBIDA)
            .transitarPara(StatusOrdemServicoEnum.CANCELADA)
        assertEquals(StatusOrdemServicoEnum.CANCELADA, novo.valor)
    }

    @Test
    fun `deve permitir cancelamento de EM_DIAGNOSTICO`() {
        val novo = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.EM_DIAGNOSTICO)
            .transitarPara(StatusOrdemServicoEnum.CANCELADA)
        assertEquals(StatusOrdemServicoEnum.CANCELADA, novo.valor)
    }

    @Test
    fun `deve permitir cancelamento de AGUARDANDO_APROVACAO`() {
        val novo = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO)
            .transitarPara(StatusOrdemServicoEnum.CANCELADA)
        assertEquals(StatusOrdemServicoEnum.CANCELADA, novo.valor)
    }

    @Test
    fun `deve permitir cancelamento de EM_EXECUCAO`() {
        val novo = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.EM_EXECUCAO)
            .transitarPara(StatusOrdemServicoEnum.CANCELADA)
        assertEquals(StatusOrdemServicoEnum.CANCELADA, novo.valor)
    }

    @Test
    fun `não deve pular status RECEBIDA direto para FINALIZADA`() {
        assertThrows<IllegalStateException> {
            StatusOrdemServico.deEnum(StatusOrdemServicoEnum.RECEBIDA)
                .transitarPara(StatusOrdemServicoEnum.FINALIZADA)
        }
    }

    @Test
    fun `não deve permitir transição a partir de ENTREGUE`() {
        assertThrows<IllegalStateException> {
            StatusOrdemServico.deEnum(StatusOrdemServicoEnum.ENTREGUE)
                .transitarPara(StatusOrdemServicoEnum.CANCELADA)
        }
    }

    @Test
    fun `não deve permitir transição a partir de CANCELADA`() {
        assertThrows<IllegalStateException> {
            StatusOrdemServico.deEnum(StatusOrdemServicoEnum.CANCELADA)
                .transitarPara(StatusOrdemServicoEnum.RECEBIDA)
        }
    }

    @Test
    fun `não deve voltar status de EM_EXECUCAO para EM_DIAGNOSTICO`() {
        assertThrows<IllegalStateException> {
            StatusOrdemServico.deEnum(StatusOrdemServicoEnum.EM_EXECUCAO)
                .transitarPara(StatusOrdemServicoEnum.EM_DIAGNOSTICO)
        }
    }
}
