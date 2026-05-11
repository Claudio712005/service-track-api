package br.com.servicetrack.domain.veiculo

import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ImagemUrl
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.Placa
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class VeiculoTest {

    private fun buildVeiculo(): Veiculo {
        return Veiculo.criar(
            proprietarioId = UsuarioId.gerar(),
            placa = Placa("ABC1D23"),
            modelo = "Civic",
            marca = "Honda",
            ano = 2020
        )
    }

    @Test
    fun `deve criar veiculo valido`() {
        val veiculo = buildVeiculo()

        val dados = veiculo.obterDados()

        assertEquals("Civic", dados.modelo)
        assertEquals("Honda", dados.marca)
        assertEquals(2020, dados.ano)
    }

    @Test
    fun `deve lançar exceção ao criar com modelo vazio`() {
        val exception = assertThrows<DomainException> {
            Veiculo.criar(
                proprietarioId = UsuarioId.gerar(),
                placa = Placa("ABC1D23"),
                modelo = "",
                marca = "Honda",
                ano = 2020
            )
        }

        assertEquals("Modelo não pode ser vazio", exception.message)
    }

    @Test
    fun `deve lançar exceção ao criar com marca vazia`() {
        val exception = assertThrows<DomainException> {
            Veiculo.criar(
                proprietarioId = UsuarioId.gerar(),
                placa = Placa("ABC1D23"),
                modelo = "Civic",
                marca = "",
                ano = 2020
            )
        }

        assertEquals("Marca não pode ser vazia", exception.message)
    }

    @Test
    fun `deve lançar exceção ao criar com ano invalido`() {
        val exception = assertThrows<DomainException> {
            Veiculo.criar(
                proprietarioId = UsuarioId.gerar(),
                placa = Placa("ABC1D23"),
                modelo = "Civic",
                marca = "Honda",
                ano = 1800
            )
        }

        assertEquals("Ano inválido", exception.message)
    }

    @Test
    fun `deve alterar placa`() {
        val veiculo = buildVeiculo()

        veiculo.alterarPlaca(Placa("XYZ1A23"))

        val dados = veiculo.obterDados()

        assertEquals("XYZ1A23", dados.placa.valor)
    }

    @Test
    fun `deve lançar exceção ao alterar para mesma placa`() {
        val veiculo = buildVeiculo()

        val exception = assertThrows<DomainException> {
            veiculo.alterarPlaca(Placa("ABC1D23"))
        }

        assertEquals("A nova placa deve ser diferente da atual", exception.message)
    }

    @Test
    fun `deve alterar dados do veiculo`() {
        val veiculo = buildVeiculo()

        veiculo.alterarDados(
            modelo = "Corolla",
            marca = "Toyota",
            ano = 2022
        )

        val dados = veiculo.obterDados()

        assertEquals("Corolla", dados.modelo)
        assertEquals("Toyota", dados.marca)
        assertEquals(2022, dados.ano)
    }

    @Test
    fun `deve lançar exceção ao alterar dados com modelo vazio`() {
        val veiculo = buildVeiculo()

        val exception = assertThrows<DomainException> {
            veiculo.alterarDados("", "Toyota", 2022)
        }

        assertEquals("Modelo não pode ser vazio", exception.message)
    }

    @Test
    fun `deve lançar exceção ao alterar dados com marca vazia`() {
        val veiculo = buildVeiculo()

        val exception = assertThrows<DomainException> {
            veiculo.alterarDados("Corolla", "", 2022)
        }

        assertEquals("Marca não pode ser vazia", exception.message)
    }

    @Test
    fun `deve lançar exceção ao alterar dados com ano invalido`() {
        val veiculo = buildVeiculo()

        val exception = assertThrows<DomainException> {
            veiculo.alterarDados("Corolla", "Toyota", 1800)
        }

        assertEquals("Ano inválido", exception.message)
    }

    @Test
    fun `deve verificar que veiculo pertence ao usuario`() {
        val usuarioId = UsuarioId.gerar()

        val veiculo = Veiculo.criar(
            proprietarioId = usuarioId,
            placa = Placa("ABC1D23"),
            modelo = "Civic",
            marca = "Honda",
            ano = 2020
        )

        assertTrue(veiculo.pertenceAoUsuario(usuarioId))
    }

    @Test
    fun `deve verificar que veiculo nao pertence ao usuario`() {
        val veiculo = buildVeiculo()

        val outroUsuario = UsuarioId.gerar()

        assertFalse(veiculo.pertenceAoUsuario(outroUsuario))
    }

    @Test
    fun `deve atualizar proprietario`() {
        val veiculo = buildVeiculo()
        val novoProprietario = UsuarioId.gerar()

        veiculo.atualizarProprietario(novoProprietario)

        assertTrue(veiculo.pertenceAoUsuario(novoProprietario))
    }

    @Test
    fun `deve lançar exceção ao atualizar para mesmo proprietario`() {
        val usuarioId = UsuarioId.gerar()

        val veiculo = Veiculo.criar(
            proprietarioId = usuarioId,
            placa = Placa("ABC1D23"),
            modelo = "Civic",
            marca = "Honda",
            ano = 2020
        )

        val exception = assertThrows<DomainException> {
            veiculo.atualizarProprietario(usuarioId)
        }

        assertEquals("O veículo já pertence a este usuário", exception.message)
    }

    @Test
    fun `deve desativar veiculo ativo`() {
        val veiculo = buildVeiculo()

        veiculo.desativarVeiculo()

        val exception = assertThrows<DomainException> {
            veiculo.desativarVeiculo()
        }
        assertEquals("O veículo já está desativado", exception.message)
    }

    @Test
    fun `deve lançar exceção ao alterar placa de veiculo desativado`() {
        val veiculo = buildVeiculo()
        veiculo.desativarVeiculo()

        val exception = assertThrows<DomainException> {
            veiculo.alterarPlaca(Placa("XYZ1A23"))
        }
        assertEquals("Veículo desativado não pode ter a placa alterada", exception.message)
    }

    @Test
    fun `deve lançar exceção ao alterar dados de veiculo desativado`() {
        val veiculo = buildVeiculo()
        veiculo.desativarVeiculo()

        val exception = assertThrows<DomainException> {
            veiculo.alterarDados("Corolla", "Toyota", 2022)
        }
        assertEquals("Veículo desativado não pode ter os dados alterados", exception.message)
    }

    @Test
    fun `deve lançar exceção ao atualizar proprietario de veiculo desativado`() {
        val veiculo = buildVeiculo()
        veiculo.desativarVeiculo()

        val exception = assertThrows<DomainException> {
            veiculo.atualizarProprietario(UsuarioId.gerar())
        }
        assertTrue(exception.message!!.equals("Veículo inativo não pode ter o proprietário alterado"))
    }

    @Test
    fun `deve criar veiculo com imagemUrl`() {
        val imagemUrl = ImagemUrl.criar("https://images.unsplash.com/photo-abc")
        val veiculo = Veiculo.criar(
            proprietarioId = UsuarioId.gerar(),
            placa = Placa("ABC1D23"),
            modelo = "Civic",
            marca = "Honda",
            ano = 2020,
            imagemUrl = imagemUrl
        )
        assertEquals("https://images.unsplash.com/photo-abc", veiculo.obterDados().imagemUrl?.url)
    }

    @Test
    fun `deve definir imagemUrl em veiculo ativo`() {
        val veiculo = buildVeiculo()
        val imagemUrl = ImagemUrl.criar("https://images.unsplash.com/photo-xyz")

        veiculo.definirImagemUrl(imagemUrl)

        assertEquals("https://images.unsplash.com/photo-xyz", veiculo.obterDados().imagemUrl?.url)
    }

    @Test
    fun `deve definir imagemUrl como nula em veiculo ativo`() {
        val veiculo = buildVeiculo()

        veiculo.definirImagemUrl(null)

        assertNull(veiculo.obterDados().imagemUrl)
    }

    @Test
    fun `deve lançar exceção ao definir imagemUrl em veiculo desativado`() {
        val veiculo = buildVeiculo()
        veiculo.desativarVeiculo()

        val exception = assertThrows<DomainException> {
            veiculo.definirImagemUrl(ImagemUrl.criar("https://images.unsplash.com/photo-xyz"))
        }
        assertEquals("Veículo desativado não pode ter a imagem alterada", exception.message)
    }

    @Test
    fun `deve reconstituir veiculo com imagemUrl e codigoFipe`() {
        val id = br.com.servicetrack.domain.veiculo.vo.VeiculoId.gerar()
        val proprietarioId = UsuarioId.gerar()
        val agora = java.time.LocalDateTime.now()
        val imagemUrl = ImagemUrl.criar("https://images.unsplash.com/photo-abc")

        val veiculo = Veiculo.reconstituir(
            id = id,
            marca = "Toyota",
            placa = Placa("XYZ1A23"),
            ano = 2022,
            proprietarioId = proprietarioId,
            modelo = "Corolla",
            dataCriacao = agora,
            dataAtualizacao = agora,
            ativo = br.com.servicetrack.domain.shared.enums.IndicativoSimNao.S,
            imagemUrl = imagemUrl,
            codigoFipe = "006009-1"
        )

        val dados = veiculo.obterDados()
        assertEquals("https://images.unsplash.com/photo-abc", dados.imagemUrl?.url)
        assertEquals("006009-1", dados.codigoFipe)
    }

    @Test
    fun `deve reconstituir veiculo a partir de dados de persistencia`() {
        val id = br.com.servicetrack.domain.veiculo.vo.VeiculoId.gerar()
        val proprietarioId = UsuarioId.gerar()
        val agora = java.time.LocalDateTime.now()

        val veiculo = Veiculo.reconstituir(
            id = id,
            marca = "Honda",
            placa = Placa("ABC1D23"),
            ano = 2020,
            proprietarioId = proprietarioId,
            modelo = "Civic",
            dataCriacao = agora,
            dataAtualizacao = agora,
            ativo = br.com.servicetrack.domain.shared.enums.IndicativoSimNao.S
        )

        assertEquals(id, veiculo.id)
        val dados = veiculo.obterDados()
        assertEquals("Honda", dados.marca)
        assertEquals("Civic", dados.modelo)
        assertEquals(2020, dados.ano)
        assertEquals(proprietarioId, dados.proprietarioId)
    }
}