package br.com.servicetrack.infrastructure.notificacao.template

import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.shared.exception.DomainException
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class QuteTemplateRendererAdapterIT {

    @Inject
    lateinit var renderer: QuteTemplateRendererAdapter

    @Test
    fun `deve renderizar template MUDANCA_STATUS_OS com variaveis`() {
        val variaveis = VariaveisTemplate.de(
            mapOf(
                "os" to "12345",
                "novoStatus" to "DIAGNOSTICO",
                "nomeCliente" to "Cláudio",
                "observacao" to "Aguardando peças",
            ),
        )

        val resultado = renderer.renderizar(
            TipoConteudoNotificacao.MUDANCA_STATUS_OS,
            variaveis,
        )

        assertEquals("[ServiceTrack] OS #12345 atualizada para DIAGNOSTICO", resultado.assunto)
        assertTrue(resultado.corpoHtml.contains("Cláudio"))
        assertTrue(resultado.corpoHtml.contains("#12345"))
        assertTrue(resultado.corpoHtml.contains("DIAGNOSTICO"))
        assertTrue(resultado.corpoHtml.contains("Aguardando peças"))
        assertTrue(resultado.corpoTexto.contains("Cláudio"))
        assertTrue(resultado.corpoTexto.contains("#12345"))
    }

    @Test
    fun `deve renderizar sem observacao opcional`() {
        val variaveis = VariaveisTemplate.de(
            mapOf(
                "os" to "999",
                "novoStatus" to "CONCLUIDA",
                "nomeCliente" to "Ana",
            ),
        )

        val resultado = renderer.renderizar(
            TipoConteudoNotificacao.MUDANCA_STATUS_OS,
            variaveis,
        )

        assertTrue(resultado.corpoHtml.contains("Ana"))
        assertTrue(resultado.corpoHtml.contains("CONCLUIDA"))
    }

    @Test
    fun `todos os TipoConteudoNotificacao devem ter templates`() {
        val faltantes = mutableListOf<String>()
        TipoConteudoNotificacao.values().forEach { tipo ->
            try {
                renderer.renderizar(tipo, VariaveisTemplate.VAZIO)
            } catch (e: DomainException) {
                faltantes.add("${tipo.name}: ${e.message}")
            } catch (e: io.quarkus.qute.TemplateException) {
            }
        }
        assertTrue(
            faltantes.isEmpty(),
            "Tipos sem template completo: $faltantes",
        )
    }
}

