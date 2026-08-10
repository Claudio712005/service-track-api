package br.com.servicetrack.infrastructure.observabilidade

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.domain.shared.exception.DomainException
import java.lang.reflect.InvocationTargetException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClassificadorDeErroTest {

    @Test
    fun `excecao de aplicacao e curada e mantem a mensagem`() {
        val classificacao = ClassificadorDeErro.classificar(EntidadeNaoEncontradaException("Cliente", arrayOf("id=abc")))

        assertTrue(classificacao.curado)
        assertEquals("Entidade 'Cliente' não encontrada para os parâmetros: id=abc.", classificacao.mensagem)
    }

    @Test
    fun `excecao de dominio e curada`() {
        val classificacao = ClassificadorDeErro.classificar(DomainException("CPF invalido"))

        assertTrue(classificacao.curado)
        assertEquals("CPF invalido", classificacao.mensagem)
    }

    @Test
    fun `excecao de terceiro nao expoe a mensagem`() {
        val vazamento = IllegalStateException("duplicate key value violates unique constraint: joao@example.com")

        val classificacao = ClassificadorDeErro.classificar(vazamento)

        assertFalse(classificacao.curado)
        assertNull(classificacao.mensagem)
        assertEquals("IllegalStateException", classificacao.tipo)
    }

    @Test
    fun `desembrulha a causa de InvocationTargetException`() {
        val original = EntidadeNaoEncontradaException("Veiculo", arrayOf("placa=ABC1D23"))

        val classificacao = ClassificadorDeErro.classificar(InvocationTargetException(original))

        assertTrue(classificacao.curado)
        assertEquals("EntidadeNaoEncontradaException", classificacao.tipo)
        assertEquals("Entidade 'Veiculo' não encontrada para os parâmetros: placa=ABC1D23.", classificacao.mensagem)
    }

    @Test
    fun `mantem a excecao quando nao ha causa para desembrulhar`() {
        val erro = RuntimeException("falha")

        assertEquals(erro, ClassificadorDeErro.desembrulhar(erro))
    }
}
