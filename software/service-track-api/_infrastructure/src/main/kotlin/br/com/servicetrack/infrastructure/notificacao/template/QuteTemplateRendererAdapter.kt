package br.com.servicetrack.infrastructure.notificacao.template

import br.com.servicetrack.application.notificacao.dto.ConteudoRenderizado
import br.com.servicetrack.application.notificacao.ports.out.TemplateRendererPort
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.shared.exception.DomainException
import io.quarkus.qute.Engine
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class QuteTemplateRendererAdapter @Inject constructor(
    private val engine: Engine,
) : TemplateRendererPort {

    override fun renderizar(
        tipoConteudo: TipoConteudoNotificacao,
        variaveis: VariaveisTemplate,
    ): ConteudoRenderizado {
        val dados = variaveis.comoMap()
        return ConteudoRenderizado(
            assunto = render(tipoConteudo, "subject.txt", dados).trim(),
            corpoHtml = render(tipoConteudo, "body.html", dados),
            corpoTexto = render(tipoConteudo, "body.txt", dados),
        )
    }

    private fun render(
        tipoConteudo: TipoConteudoNotificacao,
        arquivo: String,
        dados: Map<String, String>,
    ): String {
        val caminho = "notificacao/${tipoConteudo.name}/$arquivo"
        val template = engine.getTemplate(caminho)
            ?: throw DomainException(
                "Template '$caminho' não encontrado para o tipo $tipoConteudo",
            )
        var instance = template.instance()
        dados.forEach { (chave, valor) -> instance = instance.data(chave, valor) }
        return instance.render()
    }
}

