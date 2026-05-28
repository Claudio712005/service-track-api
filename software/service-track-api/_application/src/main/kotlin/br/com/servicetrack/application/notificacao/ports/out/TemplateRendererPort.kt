package br.com.servicetrack.application.notificacao.ports.out

import br.com.servicetrack.application.notificacao.dto.ConteudoRenderizado
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate

/**
 * Renderiza o conteúdo de uma notificação a partir do tipo de conteúdo e das variáveis.
 * A implementação concreta usa Quarkus Qute (templates em `resources/templates/notificacao/`).
 */
interface TemplateRendererPort {

    fun renderizar(
        tipoConteudo: TipoConteudoNotificacao,
        variaveis: VariaveisTemplate,
    ): ConteudoRenderizado
}

