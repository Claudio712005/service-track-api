package br.com.servicetrack.application.mecanico.service

import br.com.servicetrack.application.exception.UsuarioJaExisteException
import br.com.servicetrack.application.mecanico.dto.request.CadastrarMecanicoReqDTO
import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import br.com.servicetrack.domain.shared.exception.DomainException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate

class CadastrarMecanicoServiceTest {

    private val usuarioRepository = mockk<UsuarioRepositoryPort>()
    private val mecanicoRepository = mockk<MecanicoRepositoryPort>()
    private val criptografia = mockk<CriptografiaPort>()

    private val service = CadastrarMecanicoService(usuarioRepository, mecanicoRepository, criptografia)

    private val requisicao = CadastrarMecanicoReqDTO(
        nome = "João da Silva",
        email = "joao@mecanica.com",
        senha = "#Tiee123456",
        telefone = "11999999999",
        cpf = "14716682072",
        dataNascimento = LocalDate.of(1985, 3, 20),
        nivel = NivelMecanicoEnum.JUNIOR,
        valorHora = BigDecimal("50.00")
    )

    @Test
    fun `deve cadastrar mecanico com sucesso e retornar DTO com dados corretos`() {
        every { usuarioRepository.existePorEmailOuCpf(any(), any()) } returns false
        every { criptografia.criptografar(any()) } returns "hash"
        every { usuarioRepository.salvar(any()) } returns Unit
        every { mecanicoRepository.salvar(any()) } returns Unit

        val response = service.cadastrarMecanico(requisicao)

        assertNotNull(response)
        assertNotNull(response.usuarioId)
        assertEquals(requisicao.nome, response.nome)
        assertEquals(requisicao.email, response.email)
        assertEquals(requisicao.cpf, response.cpf)
        assertEquals(NivelMecanicoEnum.JUNIOR.name, response.nivel)
        assertEquals(BigDecimal("50.00"), response.valorHora)
        assertEquals(true, response.ativo)
        verify(exactly = 1) { usuarioRepository.salvar(any()) }
        verify(exactly = 1) { mecanicoRepository.salvar(any()) }
    }

    @Test
    fun `deve lancar excecao quando email ou cpf ja estiver cadastrado`() {
        every { usuarioRepository.existePorEmailOuCpf(any(), any()) } returns true

        assertThrows<UsuarioJaExisteException> {
            service.cadastrarMecanico(requisicao)
        }

        verify(exactly = 0) { usuarioRepository.salvar(any()) }
        verify(exactly = 0) { mecanicoRepository.salvar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando senha nao atender politica`() {
        every { usuarioRepository.existePorEmailOuCpf(any(), any()) } returns false

        val reqSenhaFraca = requisicao.copy(senha = "fraca")

        assertThrows<DomainException> {
            service.cadastrarMecanico(reqSenhaFraca)
        }

        verify(exactly = 0) { usuarioRepository.salvar(any()) }
        verify(exactly = 0) { mecanicoRepository.salvar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando valor hora for zero ou negativo`() {
        every { usuarioRepository.existePorEmailOuCpf(any(), any()) } returns false
        every { criptografia.criptografar(any()) } returns "hash"

        val reqValorInvalido = requisicao.copy(valorHora = BigDecimal.ZERO)

        assertThrows<DomainException> {
            service.cadastrarMecanico(reqValorInvalido)
        }

        verify(exactly = 0) { mecanicoRepository.salvar(any()) }
    }
}
