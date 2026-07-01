package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.MarcaInvalidaFipeException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.dto.fipe.MarcaFipeDTO
import br.com.servicetrack.application.veiculo.dto.request.AtualizarVeiculoReqDTO
import br.com.servicetrack.application.veiculo.ports.out.FipePort
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.shared.enums.IndicativoSimNao
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.Placa
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

class AtualizarVeiculoServiceTest {

    private val repository = mockk<VeiculoRepositoryPort>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val jwt = mockk<JwtPort>()
    private val fipe = mockk<FipePort>()

    private val service = AtualizarVeiculoService(repository, usuarioRepository, jwt, fipe)

    private val proprietarioId = UsuarioId.gerar()
    private val veiculoId = VeiculoId.gerar()

    private val marcasFipe = listOf(
        MarcaFipeDTO(codigo = "23", nome = "Honda"),
        MarcaFipeDTO(codigo = "59", nome = "Toyota")
    )

    private fun buildCliente(id: UsuarioId = UsuarioId.gerar()): Usuario = Usuario.reconstituir(
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
        dataAtualizacao = LocalDateTime.now()
    )

    private fun buildMecanico(id: UsuarioId = UsuarioId.gerar()): Usuario = Usuario.reconstituir(
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
        dataAtualizacao = LocalDateTime.now()
    )

    private fun buildVeiculo(proprietario: UsuarioId = proprietarioId): Veiculo =
        Veiculo.reconstituir(
            id = veiculoId,
            marca = "Honda",
            placa = Placa("ABC1D23"),
            ano = 2020,
            proprietarioId = proprietario,
            modelo = "Civic",
            dataCriacao = LocalDateTime.now(),
            dataAtualizacao = LocalDateTime.now(),
            ativo = IndicativoSimNao.S
        )

    @Test
    fun `deve atualizar veiculo quando proprietario faz a requisicao`() {
        val proprietario = buildCliente(proprietarioId)
        val veiculo = buildVeiculo(proprietarioId)
        val req = AtualizarVeiculoReqDTO(placa = null, modelo = "Fit", marca = "Honda", ano = 2022)

        every { jwt.getUsuarioId() } returns proprietarioId
        every { usuarioRepository.buscarPorId(proprietarioId) } returns proprietario
        every { repository.buscarPorId(veiculoId) } returns veiculo
        every { fipe.listarMarcasCarros() } returns marcasFipe
        every { repository.atualizar(any()) } returns Unit

        val result = service.atualizarVeiculo(veiculoId, req)

        assertEquals("Fit", result.modelo)
        assertEquals(2022, result.ano)
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve atualizar veiculo quando mecanico faz a requisicao`() {
        val mecanicoId = UsuarioId.gerar()
        val mecanico = buildMecanico(mecanicoId)
        val veiculo = buildVeiculo(proprietarioId)
        val req = AtualizarVeiculoReqDTO(placa = null, modelo = "Corolla", marca = "Toyota", ano = 2023)

        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { repository.buscarPorId(veiculoId) } returns veiculo
        every { fipe.listarMarcasCarros() } returns marcasFipe
        every { repository.atualizar(any()) } returns Unit

        val result = service.atualizarVeiculo(veiculoId, req)

        assertEquals("Corolla", result.modelo)
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve atualizar urlImagem do veiculo sem validar FIPE quando apenas imagem e alterada`() {
        val proprietario = buildCliente(proprietarioId)
        val veiculo = buildVeiculo(proprietarioId)
        val req = AtualizarVeiculoReqDTO(urlImagem = "https://images.unsplash.com/foto-teste")

        every { jwt.getUsuarioId() } returns proprietarioId
        every { usuarioRepository.buscarPorId(proprietarioId) } returns proprietario
        every { repository.buscarPorId(veiculoId) } returns veiculo
        every { repository.atualizar(any()) } returns Unit

        val result = service.atualizarVeiculo(veiculoId, req)

        assertEquals("https://images.unsplash.com/foto-teste", result.urlImagem)
        verify(exactly = 0) { fipe.listarMarcasCarros() }
        verify(exactly = 1) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar MarcaInvalidaFipeException quando nova marca nao existe na FIPE`() {
        val proprietario = buildCliente(proprietarioId)
        val veiculo = buildVeiculo(proprietarioId)
        val req = AtualizarVeiculoReqDTO(marca = "MarcaInexistente")

        every { jwt.getUsuarioId() } returns proprietarioId
        every { usuarioRepository.buscarPorId(proprietarioId) } returns proprietario
        every { repository.buscarPorId(veiculoId) } returns veiculo
        every { fipe.listarMarcasCarros() } returns marcasFipe

        assertThrows<MarcaInvalidaFipeException> {
            service.atualizarVeiculo(veiculoId, req)
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancар EntidadeNaoEncontradaException quando usuario nao encontrado`() {
        val outroId = UsuarioId.gerar()

        every { jwt.getUsuarioId() } returns outroId
        every { usuarioRepository.buscarPorId(outroId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.atualizarVeiculo(veiculoId, AtualizarVeiculoReqDTO())
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando veiculo nao encontrado`() {
        val proprietario = buildCliente(proprietarioId)

        every { jwt.getUsuarioId() } returns proprietarioId
        every { usuarioRepository.buscarPorId(proprietarioId) } returns proprietario
        every { repository.buscarPorId(veiculoId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.atualizarVeiculo(veiculoId, AtualizarVeiculoReqDTO())
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando cliente nao e proprietario`() {
        val outroClienteId = UsuarioId.gerar()
        val outroCliente = buildCliente(outroClienteId)
        val veiculo = buildVeiculo(proprietarioId)

        every { jwt.getUsuarioId() } returns outroClienteId
        every { usuarioRepository.buscarPorId(outroClienteId) } returns outroCliente
        every { repository.buscarPorId(veiculoId) } returns veiculo

        assertThrows<OperacaoNegadaException> {
            service.atualizarVeiculo(veiculoId, AtualizarVeiculoReqDTO())
        }

        verify(exactly = 0) { repository.atualizar(any()) }
    }
}
