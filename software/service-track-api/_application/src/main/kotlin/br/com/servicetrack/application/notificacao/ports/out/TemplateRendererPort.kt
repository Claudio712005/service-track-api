package br.com.servicetrack.application.notificacao.ports.out

import br.com.servicetrack.application.notificacao.dto.ConteudoRenderizado
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate

interface TemplateRendererPort {

    fun renderizar(
        tipoConteudo: TipoConteudoNotificacao,
        variaveis: VariaveisTemplate,
    ): ConteudoRenderizado
}

