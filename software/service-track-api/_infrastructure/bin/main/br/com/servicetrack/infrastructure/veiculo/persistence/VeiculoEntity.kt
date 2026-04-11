package br.com.servicetrack.infrastructure.veiculo.persistence

import br.com.servicetrack.domain.shared.enums.IndicativoSimNao
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.Placa
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import br.com.servicetrack.infrastructure.usuario.persistence.UsuarioEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "veiculos")
class VeiculoEntity: PanacheEntityBase {

    @Id
    @Column(name = "veiculo_id", nullable = false, updatable = false)
    lateinit var id: UUID

    @Column(name = "placa", nullable = false, unique = true, updatable = false)
    lateinit var placa: String

    @Column(name = "modelo", nullable = false)
    lateinit var modelo: String

    @Column(name = "marca", nullable = false)
    lateinit var marca: String

    @Column(name = "ano", nullable = false)
    var ano: Int = 0

    @Column(name = "ativo", nullable = false)
    @Enumerated(EnumType.STRING)
    var ativo: IndicativoSimNao = IndicativoSimNao.S

    @CreationTimestamp
    @Column(nullable = false, name = "data_criacao")
    lateinit var dataCriacao: LocalDateTime

    @UpdateTimestamp
    @Column(nullable = false, name = "data_atualizacao")
    lateinit var dataAtualizacao: LocalDateTime

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietario_id", nullable = false, updatable = true)
    lateinit var proprietario: UsuarioEntity

    companion object : PanacheCompanion<VeiculoEntity>{

        fun de(veiculo: Veiculo): VeiculoEntity {
            val dados = veiculo.obterDados()

            return VeiculoEntity().apply {
                id = UUID.fromString(dados.id.valor)
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
    
    fun toDomain(): Veiculo = Veiculo.reconstituir(
        id = VeiculoId(id.toString()),
        proprietarioId = UsuarioId(proprietario.id.toString()),
        placa = Placa(placa),
        modelo = modelo,
        marca = marca,
        ano = ano,
        dataCriacao = dataCriacao,
        dataAtualizacao = dataAtualizacao,
        ativo = ativo
    )
}