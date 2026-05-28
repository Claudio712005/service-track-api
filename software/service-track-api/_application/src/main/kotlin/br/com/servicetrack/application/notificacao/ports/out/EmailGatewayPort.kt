package br.com.servicetrack.application.notificacao.ports.out

import br.com.servicetrack.application.notificacao.dto.EmailMensagem
import br.com.servicetrack.application.notificacao.dto.ResultadoEnvio

interface EmailGatewayPort {

    fun enviar(mensagem: EmailMensagem): ResultadoEnvio
}

