package br.com.servicetrack.infrastructure.auditoria.persistence

import br.com.servicetrack.domain.auditoria.Auditoria
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "auditorias")
class AuditoriaEntity : PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    lateinit var id: UUID

    @Column(name = "endereco_ip", nullable = false, length = 45)
    lateinit var enderecoIp: String

    @Column(name = "referencia_id", nullable = false, length = 36)
    lateinit var referenciaId: String

    @Column(name = "data_criacao", nullable = false, updatable = false)
    lateinit var dataCriacao: LocalDateTime

    @Column(name = "tipo_entidade", nullable = false, length = 50)
    lateinit var tipoEntidade: String

    @Column(name = "tipo_evento", nullable = false, length = 50)
    lateinit var tipoEvento: String

    @Column(name = "descricao_evento", nullable = false, columnDefinition = "TEXT")
    lateinit var descricaoEvento: String

    @Column(name = "dados", nullable = false, columnDefinition = "TEXT")
    lateinit var dados: String

    @Column(name = "responsavel_acao", nullable = false, length = 36)
    lateinit var responsavelAcao: String

    companion object : PanacheCompanion<AuditoriaEntity> {

        fun de(auditoria: Auditoria, dadosJson: String): AuditoriaEntity = AuditoriaEntity().apply {
            id = UUID.fromString(auditoria.id.value)
            enderecoIp = auditoria.enderecoIp.value
            referenciaId = auditoria.referenciaId.value
            dataCriacao = auditoria.dataCriacao
            tipoEntidade = auditoria.eventoAuditoria.entidade.name
            tipoEvento = auditoria.eventoAuditoria.tipo.name
            descricaoEvento = auditoria.eventoAuditoria.descricao
            dados = dadosJson
            responsavelAcao = auditoria.responsavelAcao.valor
        }
    }
}
