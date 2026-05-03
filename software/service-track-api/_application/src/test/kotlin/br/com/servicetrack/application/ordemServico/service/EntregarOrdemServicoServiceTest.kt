package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

class EntregarOrdemServicoServiceTest {

    private val osRepository = mockk<OrdemServicoRepositoryPort>()
    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val jwt = mockk<JwtPort>()

    private val service = EntregarOrdemServicoService(osRepository, usuarioRepository, jwt)

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

    private fun buildOs(status: StatusOrdemServicoEnum = StatusOrdemServicoEnum.FINALIZADA): OrdemServico =
        OrdemServico.reconstituir(
            id = OrdemServicoId.gerar(),
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
            itensServico = mutableListOf(),
        )

    @Test
    fun `deve registrar entrega quando mecanico faz a requisicao`() {
        val mecanico = buildMecanico()
        val os = buildOs()
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns mecanico
        every { osRepository.buscarPorId(any()) } returns os
        every { osRepository.atualizar(any()) } answers { firstArg() }

        val result = service.entregarOrdemServico(os.id.valor)

        assertEquals(StatusOrdemServicoEnum.ENTREGUE, result.status)
        verify(exactly = 1) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar EntidadeNaoEncontradaException quando solicitante nao encontrado`() {
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns null

        assertThrows<EntidadeNaoEncontradaException> {
            service.entregarOrdemServico(OrdemServicoId.gerar().valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }

    @Test
    fun `deve lancar OperacaoNegadaException quando solicitante e um cliente`() {
        val cliente = buildCliente(mecanicoId)
        every { jwt.getUsuarioId() } returns mecanicoId
        every { usuarioRepository.buscarPorId(mecanicoId) } returns cliente

        assertThrows<OperacaoNegadaException> {
            service.entregarOrdemServico(OrdemServicoId.gerar().valor)
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
            service.entregarOrdemServico(OrdemServicoId.gerar().valor)
        }

        verify(exactly = 0) { osRepository.atualizar(any()) }
    }
}
