package br.com.servicetrack.application.notificacao.service

import br.com.servicetrack.application.annotation.ApplicationService
import br.com.servicetrack.application.notificacao.dto.EnfileirarNotificacaoCommand
import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.domain.notificacao.Notificacao
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId

@ApplicationService
class EnfileirarNotificacaoUseCaseImpl(
    private val repository: NotificacaoRepositoryPort,
) : EnfileirarNotificacaoUseCase {

    override fun executar(comando: EnfileirarNotificacaoCommand): NotificacaoId {
        val notificacao = Notificacao.gerar(
            assunto = comando.assunto,
            titulo = comando.titulo,
            descricao = comando.descricao,
            variaveis = comando.variaveis,
            tipoNotificacao = comando.tipoNotificacao,
            tipoConteudoNotificacao = comando.tipoConteudoNotificacao,
            destinatario = comando.destinatario,
            copias = comando.copias,
        )
        repository.salvar(notificacao)
        return notificacao.id
    }
}

