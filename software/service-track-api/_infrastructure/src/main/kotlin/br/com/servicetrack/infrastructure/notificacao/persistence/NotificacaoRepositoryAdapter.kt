package br.com.servicetrack.infrastructure.notificacao.persistence

import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.domain.notificacao.Notificacao
import br.com.servicetrack.domain.notificacao.StatusEnvio
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class NotificacaoRepositoryAdapter(
    private val objectMapper: ObjectMapper,
) : NotificacaoRepositoryPort {

    override fun salvar(notificacao: Notificacao) {
        NotificacaoEntity.de(notificacao, objectMapper).persist()
    }

    override fun atualizar(notificacao: Notificacao) {
        val entity = NotificacaoEntity
            .find("id", UUID.fromString(notificacao.id.value))
            .firstResult() ?: return
        entity.atualizarDe(notificacao, objectMapper)
    }

    override fun buscarPorId(id: NotificacaoId): Notificacao? {
        val entity = NotificacaoEntity
            .find("id", UUID.fromString(id.value))
            .firstResult() ?: return null
        return entity.toDomain(objectMapper)
    }

    override fun buscarPendentesParaEnvio(limite: Int): List<Notificacao> {
        return NotificacaoEntity
            .find("statusEnvio", StatusEnvio.PENDENTE)
            .page(0, limite)
            .list()
            .map { it.toDomain(objectMapper) }
    }
}
