package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.exception.UsuarioJaExisteException
import br.com.servicetrack.application.usuario.dto.request.CadastrarClienteReqDTO
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Cpf
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone
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
        every { repository.buscarInativoPorCpf(any()) } returns null
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
    fun `deve reativar usuario inativo quando cpf ja existia desativado`() {
        val usuarioInativo = Usuario.reconstituir(
            id = br.com.servicetrack.domain.usuario.vo.UsuarioId.gerar(),
            nome = "Nome Antigo",
            email = Email("antigo@email.com"),
            senhaHash = Senha.deHash("hashantigo"),
            dataNascimento = LocalDate.of(2000, 1, 1),
            telefone = Telefone("11900000000"),
            cpf = Cpf(requisicao.cpf),
            ativo = false,
            roles = setOf(Role.CLIENTE),
            dataCriacao = java.time.LocalDateTime.now(),
            dataAtualizacao = java.time.LocalDateTime.now()
        )

        every { repository.existePorEmailOuCpf(any(), any()) } returns false
        every { repository.buscarInativoPorCpf(requisicao.cpf) } returns usuarioInativo
        every { criptografia.criptografar(any()) } returns "novohash"
        every { repository.atualizar(any()) } returns Unit

        val response = service.criarUsuario(requisicao)

        assertNotNull(response)
        assertEquals(requisicao.nome, response.nome)
        assertEquals(requisicao.email, response.email)
        assertEquals(true, response.ativo)
        verify(exactly = 0) { repository.salvar(any()) }
        verify(exactly = 1) { repository.atualizar(any()) }
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
