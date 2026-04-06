package br.com.servicetrack.infrastructure.mecanico.persistence

import br.com.servicetrack.domain.mecanico.Mecanico
import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "mecanicos")
class MecanicoEntity : PanacheEntityBase {

    @Id
    @Column(name = "usuario_id", nullable = false, updatable = false)
    lateinit var usuarioId: UUID

    @Column(name = "valor_hora", nullable = false, precision = 10, scale = 2)
    lateinit var valorHora: BigDecimal

    @Column(name = "nivel", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var nivel: NivelMecanicoEnum

    companion object : PanacheCompanion<MecanicoEntity> {

        fun de(mecanico: Mecanico): MecanicoEntity = MecanicoEntity().apply {
            usuarioId = UUID.fromString(mecanico.usuarioId.valor)
            valorHora = mecanico.obterValorHora().valor
            nivel = mecanico.obterNivel().valor
        }
    }
}
