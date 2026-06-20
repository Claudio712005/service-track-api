package br.com.servicetrack.application.notificacao.ports.`in`

import br.com.servicetrack.application.notificacao.dto.ContadorNaoLidasResDTO

interface ContarNotificacoesNaoLidasUseCase {

    fun executar(): ContadorNaoLidasResDTO
}
