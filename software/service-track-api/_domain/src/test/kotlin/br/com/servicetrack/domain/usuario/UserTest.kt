package br.com.servicetrack.domain.usuario

import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.vo.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.*

class UsuarioTest {

    private fun buildUsuario(): Usuario {
        return Usuario.criar(
            nome = "Cláudio",
            email = Email("teste@email.com"),
            senha = Senha.criar("Forte@123"),
            dataNascimento = LocalDate.of(2000, 1, 1),
            telefone = Telefone("11999999999"),
            cpf = Cpf("12345678909"),
            roles = setOf(Role.CLIENTE)
        )
    }

    @Test
    fun `deve criar usuario válido`() {
        val usuario = buildUsuario()

        assertNotNull(usuario)
        assertTrue(usuario.ehCliente())
    }

    @Test
    fun `deve lançar exceção ao criar usuario com nome vazio`() {
        val exception = assertThrows<IllegalArgumentException> {
            Usuario.criar(
                nome = "",
                email = Email("teste@email.com"),
                senha = Senha.criar("Forte@123"),
                dataNascimento = LocalDate.of(2000, 1, 1),
                telefone = Telefone("11999999999"),
                cpf = Cpf("12345678909"),
                roles = setOf(Role.CLIENTE)
            )
        }

        assertEquals("Nome não pode ser vazio", exception.message)
    }

    @Test
    fun `deve lançar exceção ao criar usuario sem roles`() {
        val exception = assertThrows<IllegalArgumentException> {
            Usuario.criar(
                nome = "Cláudio",
                email = Email("teste@email.com"),
                senha = Senha.criar("Forte@123"),
                dataNascimento = LocalDate.of(2000, 1, 1),
                telefone = Telefone("11999999999"),
                cpf = Cpf("12345678909"),
                roles = emptySet()
            )
        }

        assertEquals("Usuário deve possuir pelo menos um perfil", exception.message)
    }

    @Test
    fun `deve desativar usuario`() {
        val usuario = buildUsuario()

        usuario.desativar()

        assertFalse(usuario.ehCliente() && usuario.ehMecanico())
    }

    @Test
    fun `deve lançar exceção ao desativar usuario já desativado`() {
        val usuario = buildUsuario()
        usuario.desativar()

        val exception = assertThrows<IllegalStateException> {
            usuario.desativar()
        }

        assertEquals("Usuário já está desativado", exception.message)
    }

    @Test
    fun `deve ativar usuario`() {
        val usuario = buildUsuario()
        usuario.desativar()

        usuario.ativar()

        assertTrue(usuario.ehCliente())
    }

    @Test
    fun `deve lançar exceção ao ativar usuario já ativo`() {
        val usuario = buildUsuario()

        val exception = assertThrows<IllegalStateException> {
            usuario.ativar()
        }

        assertEquals("Usuário já está ativo", exception.message)
    }

    @Test
    fun `deve alterar senha`() {
        val usuario = buildUsuario()
        val novaSenha = Senha.deHash("\$2a\$10\$xyzAbcDef1234567890ABCDE")

        usuario.alterarSenha(novaSenha)

        assertNotNull(usuario)
    }

    @Test
    fun `deve adicionar nova role`() {
        val usuario = buildUsuario()

        usuario.adicionarRole(Role.MECANICO)

        assertTrue(usuario.ehMecanico())
    }

    @Test
    fun `deve reconhecer usuario como cliente`() {
        val usuario = buildUsuario()

        assertTrue(usuario.ehCliente())
        assertFalse(usuario.ehMecanico())
    }

    @Test
    fun `deve reconhecer usuario como mecanico quando possuir role`() {
        val usuario = buildUsuario()
        usuario.adicionarRole(Role.MECANICO)

        assertTrue(usuario.ehMecanico())
    }

    @Test
    fun `deve expor dados do usuario via obterDados`() {
        val usuario = buildUsuario()

        val dados = usuario.obterDados()

        assertEquals("Cláudio", dados.nome)
        assertEquals("teste@email.com", dados.email.valor)
        assertTrue(dados.roles.contains(Role.CLIENTE))
        assertTrue(dados.ativo)
    }
}
