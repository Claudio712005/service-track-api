package br.com.servicetrack.application.notificacao.event

import br.com.servicetrack.application.notificacao.dto.EnfileirarNotificacaoCommand
import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.enterprise.event.TransactionPhase

@ApplicationScoped
open class OrdemServicoStatusAlteradoListener(
    private val enfileirar: EnfileirarNotificacaoUseCase,
    private val usuarioRepository: UsuarioRepositoryPort,
) {

    open fun aoAlterarStatus(
        @Observes(during = TransactionPhase.AFTER_SUCCESS) evento: OrdemServicoStatusAlteradoEvent,
    ) {
        val nomeCliente = usuarioRepository.buscarPorId(evento.clienteId)
            ?.obterDados()
            ?.nome
            ?: return

        val variaveis = VariaveisTemplate.de(
            mapOf(
                "os" to evento.ordemServicoId.valor,
                "novoStatus" to evento.novoStatus.descricao,
                "nomeCliente" to nomeCliente,
            ),
        )

        val comando = EnfileirarNotificacaoCommand(
            assunto = AssuntoNotificacao("Atualização da sua OS ${evento.ordemServicoId.valor}"),
            titulo = TituloNotificacao(TipoConteudoNotificacao.MUDANCA_STATUS_OS.titulo),
            descricao = DescricaoNotificacao(
                "Status atualizado para ${evento.novoStatus.descricao}",
            ),
            variaveis = variaveis,
            tipoNotificacao = TipoNotificacao.EMAIL,
            tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
            destinatario = evento.clienteId,
        )

        enfileirar.executar(comando)
    }
}
