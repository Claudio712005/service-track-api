package br.com.servicetrack.infrastructure.notificacao.persistence

import br.com.servicetrack.domain.notificacao.Notificacao
import br.com.servicetrack.domain.notificacao.StatusEnvio
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "notificacoes")
class NotificacaoEntity : PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    lateinit var id: UUID

    @Column(name = "assunto", nullable = false, length = 150)
    lateinit var assunto: String

    @Column(name = "titulo", nullable = false, length = 200)
    lateinit var titulo: String

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    lateinit var descricao: String

    @Column(name = "variaveis_json", nullable = false, columnDefinition = "TEXT")
    lateinit var variaveisJson: String

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_notificacao", nullable = false, length = 30)
    lateinit var tipoNotificacao: TipoNotificacao

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conteudo_notificacao", nullable = false, length = 60)
    lateinit var tipoConteudoNotificacao: TipoConteudoNotificacao

    @Column(name = "destinatario_id", nullable = false)
    lateinit var destinatarioId: UUID

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "notificacao_copias",
        joinColumns = [JoinColumn(name = "notificacao_id")],
    )
    @Column(name = "usuario_id", nullable = false)
    var copias: MutableList<UUID> = mutableListOf()

    @Column(name = "data_criacao", nullable = false, updatable = false)
    lateinit var dataCriacao: LocalDateTime

    @Enumerated(EnumType.STRING)
    @Column(name = "status_envio", nullable = false, length = 20)
    lateinit var statusEnvio: StatusEnvio

    @Column(name = "data_envio")
    var dataEnvio: LocalDateTime? = null

    @Column(name = "visualizada", nullable = false)
    var visualizada: Boolean = false

    @Column(name = "data_visualizacao")
    var dataVisualizacao: LocalDateTime? = null

    @Column(name = "tentativas_envio", nullable = false)
    var tentativasEnvio: Int = 0

    @Column(name = "ultimo_erro", columnDefinition = "TEXT")
    var ultimoErro: String? = null

    fun toDomain(objectMapper: ObjectMapper): Notificacao {
        val variaveisMap: Map<String, String> = objectMapper.readValue(
            variaveisJson,
            object : TypeReference<Map<String, String>>() {},
        )
        return Notificacao.restaurar(
            id = NotificacaoId.de(id),
            assunto = AssuntoNotificacao(assunto),
            titulo = TituloNotificacao(titulo),
            descricao = DescricaoNotificacao(descricao),
            variaveis = VariaveisTemplate.de(variaveisMap),
            tipoNotificacao = tipoNotificacao,
            tipoConteudoNotificacao = tipoConteudoNotificacao,
            destinatario = UsuarioId(destinatarioId.toString()),
            copias = copias.map { UsuarioId(it.toString()) },
            dataCriacao = dataCriacao,
            statusEnvio = statusEnvio,
            dataEnvio = dataEnvio,
            visualizada = visualizada,
            dataVisualizacao = dataVisualizacao,
            tentativasEnvio = tentativasEnvio,
            ultimoErro = ultimoErro,
        )
    }

    fun atualizarDe(notificacao: Notificacao, objectMapper: ObjectMapper) {
        assunto = notificacao.assunto.valor
        titulo = notificacao.titulo.valor
        descricao = notificacao.descricao.valor
        variaveisJson = objectMapper.writeValueAsString(notificacao.variaveis.comoMap())
        tipoNotificacao = notificacao.tipoNotificacao
        tipoConteudoNotificacao = notificacao.tipoConteudoNotificacao
        destinatarioId = UUID.fromString(notificacao.destinatario.valor)
        copias = notificacao.copias.map { UUID.fromString(it.valor) }.toMutableList()
        statusEnvio = notificacao.statusEnvio
        dataEnvio = notificacao.dataEnvio
        visualizada = notificacao.visualizada
        dataVisualizacao = notificacao.dataVisualizacao
        tentativasEnvio = notificacao.tentativasEnvio
        ultimoErro = notificacao.ultimoErro
    }

    companion object : PanacheCompanion<NotificacaoEntity> {

        fun de(notificacao: Notificacao, objectMapper: ObjectMapper): NotificacaoEntity {
            val entity = NotificacaoEntity()
            entity.id = UUID.fromString(notificacao.id.value)
            entity.dataCriacao = notificacao.dataCriacao
            entity.atualizarDe(notificacao, objectMapper)
            return entity
        }
    }
}

