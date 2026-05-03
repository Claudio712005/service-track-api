package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.Placa
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BuscarVeiculoServiceTest {

    private lateinit var repository: VeiculoRepositoryPort
    private lateinit var service: BuscarVeiculoService

    @BeforeEach
    fun setup() {
        repository = mockk()
        service = BuscarVeiculoService(repository)
    }

    @Test
    fun `deve retornar veiculo quando encontrado`() {
        val veiculoId = VeiculoId.gerar()
        val proprietarioId = UsuarioId.gerar()

        val veiculo = Veiculo.criar(
            proprietarioId = proprietarioId,
            placa = Placa("ABC1D23"),
            marca = "Toyota",
            modelo = "Corolla",
            ano = 2020
        )

        every { repository.buscarPorId(veiculoId) } returns veiculo

        val resultado = service.buscarVeiculo(veiculoId)

        assertEquals(veiculo.obterDados().modelo, resultado.modelo)

        verify { repository.buscarPorId(veiculoId) }
    }

    @Test
    fun `deve lancar excecao quando veiculo nao encontrado`() {
        val veiculoId = VeiculoId.gerar()

        every { repository.buscarPorId(veiculoId) } returns null

        val exception = assertThrows(EntidadeNaoEncontradaException::class.java) {
            service.buscarVeiculo(veiculoId)
        }

        assertTrue(exception.message!!.contains("Veiculo"))

        verify { repository.buscarPorId(veiculoId) }
    }
}