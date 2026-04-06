package br.com.servicetrack.infrastructure.veiculo.persistence

import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.infrastructure.usuario.persistence.UsuarioEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "veiculos")
class VeiculoEntity: PanacheEntityBase {

    @Id
    @Column(name = "veiculo_id", nullable = false, updatable = false)
    lateinit var veiculoId: UUID

    @Column(name = "placa", nullable = false, unique = true, updatable = false)
    lateinit var placa: String

    @Column(name = "modelo", nullable = false)
    lateinit var modelo: String

    @Column(name = "marca", nullable = false)
    lateinit var marca: String

    @Column(name = "ano", nullable = false)
    var ano: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietario_id", nullable = false, updatable = true)
    lateinit var proprietario: UsuarioEntity

    companion object : PanacheCompanion<VeiculoEntity>{

        fun de(veiculo: Veiculo): VeiculoEntity {
            val dados = veiculo.obterDados()

            return VeiculoEntity().apply {
                veiculoId = dados.id.valor
                placa = dados.placa.valor
                modelo = dados.modelo
                marca = dados.marca
                ano = dados.ano
                proprietario = UsuarioEntity().apply {
                    id = UUID.fromString(dados.proprietarioId.valor)
                }
            }
        }
    }
}