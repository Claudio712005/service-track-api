package br.com.servicetrack.infrastructure.notificacao.persistence

import br.com.servicetrack.application.notificacao.dto.FiltroNotificacaoDTO
import br.com.servicetrack.application.notificacao.ports.out.NotificacaoRepositoryPort
import br.com.servicetrack.application.shared.dto.PageResDTO
import br.com.servicetrack.domain.notificacao.Notificacao
import br.com.servicetrack.domain.notificacao.StatusEnvio
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.panache.common.Sort
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

    override fun listar(filtro: FiltroNotificacaoDTO): PageResDTO<Notificacao> {
        val conditions = mutableListOf<String>()
        val params = mutableMapOf<String, Any>()

        conditions.add("destinatarioId = :destinatarioId")
        params["destinatarioId"] = UUID.fromString(filtro.destinatarioId)

        val visualizadaFiltro = filtro.visualizada
        if (visualizadaFiltro != null) {
            conditions.add("visualizada = :visualizada")
            params["visualizada"] = visualizadaFiltro
        }

        val query = conditions.joinToString(" AND ")

        val panacheQuery = NotificacaoEntity
            .find(query, Sort.descending("dataCriacao"), params)
            .page(filtro.page, filtro.size)

        val total = panacheQuery.count()
        val content = panacheQuery.list().map { it.toDomain(objectMapper) }

        return PageResDTO.de(content, filtro.page, filtro.size, total)
    }

    override fun contarNaoLidas(destinatarioId: UsuarioId): Long {
        return NotificacaoEntity.count(
            "destinatarioId = ?1 AND visualizada = ?2",
            UUID.fromString(destinatarioId.valor),
            false,
        )
    }
}
