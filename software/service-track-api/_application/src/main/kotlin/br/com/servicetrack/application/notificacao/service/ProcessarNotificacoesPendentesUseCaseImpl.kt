package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.annotation.ApplicationService
import br.com.servicetrack.application.notificacao.dto.EmailMensagem
import br.com.servicetrack.application.notificacao.dto.ResultadoEnvio
import br.com.servicetrack.application.notificacao.ports.`in`.ProcessarNotificacoesPendentesUseCase
import br.com.servicetrack.application.notificacao.ports.`in`.ProcessarNotificacoesPendentesUseCase.ResultadoLote
import br.com.servicetrack.application.notificacao.ports.out.EmailDestinatarioResolverPort
import br.com.servicetrack.application.notificacao.ports.out.EmailGatewayPort
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.notificacao.ports.out.TemplateRendererPort
import br.com.servicetrack.application.shared.ports.out.TransactionRunnerPort
import br.com.servicetrack.domain.notificacao.Notificacao

@ApplicationService
class ProcessarNotificacoesPendentesUseCaseImpl(
    private val repository: NotificacaoRepositoryPort,
    private val renderer: TemplateRendererPort,
    private val emailGateway: EmailGatewayPort,
    private val destinatarioResolver: EmailDestinatarioResolverPort,
    private val transactionRunner: TransactionRunnerPort,
) : ProcessarNotificacoesPendentesUseCase {

    override fun executar(): ResultadoLote {
        val pendentes = repository.buscarPendentesParaEnvio(limite = TAMANHO_LOTE)
        if (pendentes.isEmpty()) {
            return ResultadoLote(totalProcessado = 0, enviadas = 0, falhas = 0)
        }

        var enviadas = 0
        var falhas = 0
        pendentes.forEach { notificacao ->
            val resultado = transactionRunner.executarEmNovaTransacao {
                processarUma(notificacao)
            }
            when (resultado) {
                is ResultadoEnvio.Sucesso -> enviadas += 1
                is ResultadoEnvio.Falha -> falhas += 1
            }
        }

        return ResultadoLote(
            totalProcessado = pendentes.size,
            enviadas = enviadas,
            falhas = falhas,
        )
    }

    private fun processarUma(notificacao: Notificacao): ResultadoEnvio = try {
        val destinatarioEmail = destinatarioResolver.resolverEmail(notificacao.destinatario)
        val copiasEmails = notificacao.copias.map { destinatarioResolver.resolverEmail(it) }
        val conteudo = renderer.renderizar(
            notificacao.tipoConteudoNotificacao,
            notificacao.variaveis,
        )
        val mensagem = EmailMensagem(
            destinatario = destinatarioEmail,
            copias = copiasEmails,
            assunto = conteudo.assunto,
            corpoHtml = conteudo.corpoHtml,
            corpoTexto = conteudo.corpoTexto,
        )

        when (val resultado = emailGateway.enviar(mensagem)) {
            is ResultadoEnvio.Sucesso -> {
                notificacao.marcarComoEnviada()
                repository.atualizar(notificacao)
                resultado
            }
            is ResultadoEnvio.Falha -> {
                notificacao.registrarTentativaFalha(resultado.motivo, maxTentativas = MAX_TENTATIVAS)
                repository.atualizar(notificacao)
                resultado
            }
        }
    } catch (ex: RuntimeException) {
        runCatching {
            notificacao.registrarTentativaFalha(
                erro = ex.message ?: ex.javaClass.simpleName,
                maxTentativas = MAX_TENTATIVAS,
            )
            repository.atualizar(notificacao)
        }
        ResultadoEnvio.Falha(ex.message ?: ex.javaClass.simpleName)
    }

    companion object {
        const val TAMANHO_LOTE = 50
        const val MAX_TENTATIVAS = 3
    }
}
