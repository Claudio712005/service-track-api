package br.com.servicetrack.infrastructure.ordemServico

import br.com.servicetrack.application.exception.LinkDecisaoInvalidoException
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import io.quarkus.qute.Engine
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@ApplicationScoped
class DecisaoOrcamentoPaginaRenderer(
    private val engine: Engine,
) {

    fun processarDecisao(
        acaoConcluida: String,
        cor: String,
        icone: String,
        acao: () -> ResumoOrdemServicoResDTO,
    ): Response = try {
        val os = acao()
        val html = renderarPagina(
            titulo = "Orçamento $acaoConcluida!",
            mensagem = "Sua decisão foi registrada e o mecânico responsável já foi notificado.",
            cor = cor,
            icone = icone,
            os = os.id,
        )
        Response.ok(html).type(MediaType.TEXT_HTML).build()
    } catch (e: LinkDecisaoInvalidoException) {
        paginaErro(e.message ?: "Link inválido ou expirado")
    } catch (e: Exception) {
        paginaErro("Não foi possível processar sua decisão. O orçamento pode já ter sido respondido.")
    }

    private fun paginaErro(mensagem: String): Response = Response.status(Response.Status.BAD_REQUEST)
        .entity(
            renderarPagina(
                titulo = "Não foi possível concluir",
                mensagem = mensagem,
                cor = "#d97706",
                icone = "⚠️",
                os = null,
            ),
        )
        .type(MediaType.TEXT_HTML)
        .build()

    private fun renderarPagina(
        titulo: String,
        mensagem: String,
        cor: String,
        icone: String,
        os: String?,
    ): String {
        val template = engine.getTemplate("orcamento/resultado.html")
            ?: return "<h1>$titulo</h1><p>$mensagem</p>"
        return template.instance()
            .data("titulo", titulo)
            .data("mensagem", mensagem)
            .data("cor", cor)
            .data("icone", icone)
            .data("os", os)
            .render()
    }
}
