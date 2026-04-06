package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.exception.UsuarioJaExisteException
import br.com.servicetrack.application.usuario.dto.request.CadastrarClienteReqDTO
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class CriarUsuarioServiceTest {

    private val repository = mockk<UsuarioRepositoryPort>()
    private val criptografia = mockk<CriptografiaPort>()

    private val service = CriarUsuarioService(repository, criptografia)

    private val requisicao = CadastrarClienteReqDTO(
        nome = "Cláudio",
        email = "clausilvaaraujo11@gmail.com",
        senha = "#Tiee123456",
        telefone = "11999999999",
        cpf = "14716682072",
        dataNascimento = LocalDate.of(2005, 1, 7)
    )

    @Test
    fun `deve cadastrar novo cliente com sucesso e retornar DTO com ID`() {
        every { repository.existePorEmailOuCpf(any(), any()) } returns false
        every { criptografia.criptografar(any()) } returns "hash"
        every { repository.salvar(any()) } returns Unit

        val response = service.criarUsuario(requisicao)

        assertNotNull(response)
        assertNotNull(response.id)
        assertEquals(requisicao.nome, response.nome)
        assertEquals(requisicao.email, response.email)
        assertEquals(requisicao.cpf, response.cpf)
        assertEquals(requisicao.telefone, response.telefone)
        assertEquals(true, response.ativo)
        verify(exactly = 1) { repository.salvar(any()) }
    }

    @Test
    fun `deve lancar excecao quando email ou cpf ja estiver cadastrado`() {
        every { repository.existePorEmailOuCpf(any(), any()) } returns true

        val exception = assertThrows<UsuarioJaExisteException> {
            service.criarUsuario(requisicao)
        }

        assertNotNull(exception)
        verify(exactly = 0) { repository.salvar(any()) }
    }

    @Test
    fun `deve lancar DomainException quando senha nao atender politica`() {
        every { repository.existePorEmailOuCpf(any(), any()) } returns false

        val reqSenhaFraca = requisicao.copy(senha = "fraca")

        assertThrows<br.com.servicetrack.domain.shared.exception.DomainException> {
            service.criarUsuario(reqSenhaFraca)
        }

        verify(exactly = 0) { repository.salvar(any()) }
    }
}
