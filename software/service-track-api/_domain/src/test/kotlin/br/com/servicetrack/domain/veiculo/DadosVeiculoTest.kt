package br.com.servicetrack.domain.veiculo

import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.Placa
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class DadosVeiculoTest {

    private var veiculo: Veiculo? = null

    @BeforeEach
    internal fun setUp() {
        veiculo = Veiculo.criar(
            proprietarioId = UsuarioId.gerar(),
            placa = Placa("ABC1D23"),
            modelo = "teste",
            marca = "teste",
            ano = 2020,
        )
    }

    @Test
    fun `deve retornar dados veiculos`() {
        assertDoesNotThrow {
            veiculo?.obterDados()
        }
    }

    @Test
    fun `deve retornar dados veiculos com placa formatada`() {
        val dados = veiculo?.obterDados()
        assert(dados?.placa?.valor == "ABC1D23")
    }
}