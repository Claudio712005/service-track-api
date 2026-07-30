package br.com.servicetrack.application.observabilidade

import br.com.servicetrack.application.observabilidade.enums.CodigoUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class ContratoDeObservabilidadeTest {

    private val fontes = File("src/main/kotlin").walkTopDown().filter { it.extension == "kt" }.toList()

    private val camposSensiveis = listOf(
        "senha", "senhaAtual", "novaSenha", "confirmacaoNovaSenha",
        "email", "telefone", "dataNascimento",
    )

    @Test
    fun `todo caso de uso declara um codigo de observabilidade`() {
        val semAnotacao = mutableListOf<String>()

        fontes.filter { it.readText().contains(Regex("""class\s+\w+[^{]*?:\s*[\w, ]*UseCase""")) }
            .forEach { arquivo ->
                val linhas = arquivo.readLines()
                linhas.forEachIndexed { indice, linha ->
                    if (!linha.trimStart().startsWith("override fun ")) return@forEachIndexed
                    val anteriores = linhas.subList(maxOf(0, indice - 4), indice)
                    if (anteriores.none { it.contains("@Observavel") }) {
                        semAnotacao += "${arquivo.nameWithoutExtension}:${linha.trim()}"
                    }
                }
            }

        assertTrue(semAnotacao.isEmpty()) {
            "Casos de uso sem @Observavel: ${semAnotacao.joinToString("\n")}"
        }
    }

    @Test
    fun `nenhum campo sensivel e marcado como rastreavel`() {
        val marcados = mutableListOf<String>()

        fontes.forEach { arquivo ->
            val linhas = arquivo.readLines()
            linhas.forEachIndexed { indice, linha ->
                val campo = camposSensiveis.firstOrNull {
                    linha.trimStart().startsWith("val $it:") || linha.trimStart().startsWith("val $it ")
                } ?: return@forEachIndexed
                val anterior = linhas.getOrNull(indice - 1).orEmpty()
                if (anterior.contains("Rastreavel")) {
                    marcados += "${arquivo.nameWithoutExtension}.$campo"
                }
            }
        }

        assertTrue(marcados.isEmpty()) {
            "Campos sensiveis marcados como rastreaveis: ${marcados.joinToString(", ")}"
        }
    }

    @Test
    fun `codigos de caso de uso nao se repetem`() {
        val nomes = CodigoUseCase.entries.map { it.name }

        assertEquals(nomes.size, nomes.toSet().size)
    }

    @Test
    fun `todo codigo declara descricao e entidade`() {
        CodigoUseCase.entries.forEach {
            assertTrue(it.descricao.isNotBlank()) { "${it.name} sem descricao" }
        }
    }
}
