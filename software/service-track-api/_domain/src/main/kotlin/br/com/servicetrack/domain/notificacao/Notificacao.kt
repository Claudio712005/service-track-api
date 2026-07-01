package br.com.servicetrack.domain.notificacao

import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import java.time.LocalDateTime

@Suppress("LongParameterList")
class Notificacao private constructor(
    val id: NotificacaoId,
    val assunto: AssuntoNotificacao,
    val titulo: TituloNotificacao,
    val descricao: DescricaoNotificacao,
    val variaveis: VariaveisTemplate,
    val tipoNotificacao: TipoNotificacao,
    val tipoConteudoNotificacao: TipoConteudoNotificacao,
    val destinatario: UsuarioId,
    copias: List<UsuarioId>,
    val dataCriacao: LocalDateTime,
    statusEnvio: StatusEnvio,
    dataEnvio: LocalDateTime?,
    visualizada: Boolean,
    dataVisualizacao: LocalDateTime?,
    tentativasEnvio: Int,
    ultimoErro: String?,
) {
    val copias: List<UsuarioId> = copias.toList()

    var statusEnvio: StatusEnvio = statusEnvio
        private set

    var dataEnvio: LocalDateTime? = dataEnvio
        private set

    var visualizada: Boolean = visualizada
        private set

    var dataVisualizacao: LocalDateTime? = dataVisualizacao
        private set

    var tentativasEnvio: Int = tentativasEnvio
        private set

    var ultimoErro: String? = ultimoErro
        private set

    init {
        require(destinatario !in copias) {
            "destinatário não pode também estar em copias"
        }
        require(copias.toSet().size == copias.size) {
            "copias não pode conter UsuarioId duplicado"
        }
        require(tentativasEnvio >= 0) {
            "tentativasEnvio não pode ser negativo"
        }
        require(statusEnvio != StatusEnvio.ENVIADA || dataEnvio != null) {
            "notificação ENVIADA deve possuir dataEnvio"
        }
        require(!visualizada || dataVisualizacao != null) {
            "notificação visualizada deve possuir dataVisualizacao"
        }
        require(!visualizada || statusEnvio == StatusEnvio.ENVIADA) {
            "só é possível visualizar uma notificação ENVIADA"
        }
    }

    fun marcarComoEnviada() {
        if (statusEnvio == StatusEnvio.ENVIADA) return
        if (statusEnvio == StatusEnvio.FALHA_ENVIO) {
            throw DomainException(
                "Notificação com falha de envio não pode ser marcada como enviada sem reenvio",
            )
        }
        statusEnvio = StatusEnvio.ENVIADA
        dataEnvio = LocalDateTime.now()
        ultimoErro = null
    }

    fun marcarFalhaEnvio() {
        if (statusEnvio == StatusEnvio.ENVIADA) {
            throw DomainException("Notificação já enviada não pode ser marcada como falha")
        }
        statusEnvio = StatusEnvio.FALHA_ENVIO
    }

    fun registrarTentativaFalha(erro: String, maxTentativas: Int) {
        require(maxTentativas > 0) { "maxTentativas deve ser positivo" }
        if (statusEnvio == StatusEnvio.ENVIADA) {
            throw DomainException("Notificação já enviada não pode registrar nova tentativa")
        }
        tentativasEnvio += 1
        ultimoErro = erro
        if (tentativasEnvio >= maxTentativas) {
            statusEnvio = StatusEnvio.FALHA_ENVIO
        } else {
            statusEnvio = StatusEnvio.PENDENTE
        }
    }

    fun visualizar() {
        if (visualizada) return
        if (statusEnvio != StatusEnvio.ENVIADA) {
            throw DomainException("Só é possível visualizar uma notificação ENVIADA")
        }
        visualizada = true
        dataVisualizacao = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean =
        this === other || (other is Notificacao && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "Notificacao(id=$id, destinatario=$destinatario, statusEnvio=$statusEnvio, " +
            "tentativasEnvio=$tentativasEnvio, visualizada=$visualizada)"

    companion object {
        fun gerar(
            assunto: AssuntoNotificacao,
            titulo: TituloNotificacao,
            descricao: DescricaoNotificacao,
            variaveis: VariaveisTemplate,
            tipoNotificacao: TipoNotificacao,
            tipoConteudoNotificacao: TipoConteudoNotificacao,
            destinatario: UsuarioId,
            copias: List<UsuarioId> = emptyList(),
        ): Notificacao = Notificacao(
            id = NotificacaoId.gerar(),
            assunto = assunto,
            titulo = titulo,
            descricao = descricao,
            variaveis = variaveis,
            tipoNotificacao = tipoNotificacao,
            tipoConteudoNotificacao = tipoConteudoNotificacao,
            destinatario = destinatario,
            copias = copias,
            dataCriacao = LocalDateTime.now(),
            statusEnvio = StatusEnvio.PENDENTE,
            dataEnvio = null,
            visualizada = false,
            dataVisualizacao = null,
            tentativasEnvio = 0,
            ultimoErro = null,
        )

        @Suppress("LongParameterList")
        fun restaurar(
            id: NotificacaoId,
            assunto: AssuntoNotificacao,
            titulo: TituloNotificacao,
            descricao: DescricaoNotificacao,
            variaveis: VariaveisTemplate,
            tipoNotificacao: TipoNotificacao,
            tipoConteudoNotificacao: TipoConteudoNotificacao,
            destinatario: UsuarioId,
            copias: List<UsuarioId>,
            dataCriacao: LocalDateTime,
            statusEnvio: StatusEnvio,
            dataEnvio: LocalDateTime?,
            visualizada: Boolean,
            dataVisualizacao: LocalDateTime?,
            tentativasEnvio: Int,
            ultimoErro: String?,
        ): Notificacao = Notificacao(
            id = id,
            assunto = assunto,
            titulo = titulo,
            descricao = descricao,
            variaveis = variaveis,
            tipoNotificacao = tipoNotificacao,
            tipoConteudoNotificacao = tipoConteudoNotificacao,
            destinatario = destinatario,
            copias = copias,
            dataCriacao = dataCriacao,
            statusEnvio = statusEnvio,
            dataEnvio = dataEnvio,
            visualizada = visualizada,
            dataVisualizacao = dataVisualizacao,
            tentativasEnvio = tentativasEnvio,
            ultimoErro = ultimoErro,
        )
    }
}