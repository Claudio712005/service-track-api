package br.com.servicetrack.infrastructure.veiculo.persistence

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.shared.enums.IndicativoSimNao
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class VeiculoRepositoryAdapter : VeiculoRepositoryPort {

    override fun salvar(veiculo: Veiculo) {
        val entity = VeiculoEntity.de(veiculo)
        entity.persist()
    }

    override fun existeVeiculoPorPlaca(placa: String): Boolean =
        VeiculoEntity.count("placa = ?1 and ativo = ?2", placa, IndicativoSimNao.S) > 0

    override fun buscarPorId(id: VeiculoId): Veiculo? =
        VeiculoEntity
            .find("id = ?1 and ativo = ?2", UUID.fromString(id.valor), IndicativoSimNao.S)
            .firstResult()
            ?.toDomain()

    override fun buscarInativoPorPlaca(placa: String): Veiculo? =
        VeiculoEntity
            .find("placa = ?1 and ativo = ?2", placa, IndicativoSimNao.N)
            .firstResult()
            ?.toDomain()

    override fun desativar(id: VeiculoId) {
        val entity = VeiculoEntity
            .find("id = ?1 and ativo = ?2", UUID.fromString(id.valor), IndicativoSimNao.S)
            .firstResult()
            ?: throw EntidadeNaoEncontradaException(
                Veiculo::class.java.name,
                parametros = arrayOf(id.valor)
            )

        entity.ativo = IndicativoSimNao.N
    }

    override fun reativar(id: VeiculoId) {
        val entity = VeiculoEntity
            .find("id = ?1 and ativo = ?2", UUID.fromString(id.valor), IndicativoSimNao.N)
            .firstResult()
            ?: throw EntidadeNaoEncontradaException(
                Veiculo::class.java.name,
                parametros = arrayOf(id.valor)
            )

        entity.ativo = IndicativoSimNao.S
    }

    override fun atualizar(veiculo: Veiculo) {
        val entity = VeiculoEntity
            .find("id", UUID.fromString(veiculo.obterDados().id.valor))
            .firstResult() ?: return

        val dados = veiculo.obterDados()
        entity.placa = dados.placa.valor
        entity.modelo = dados.modelo
        entity.marca = dados.marca
        entity.ano = dados.ano
    }

    override fun listarTodos(): List<Veiculo> {
        return VeiculoEntity.list("ativo"
            , IndicativoSimNao.S
        ).map { it.toDomain() }
    }

    override fun listarPorProprietario(proprietarioId: UsuarioId): List<Veiculo> {
        return VeiculoEntity
            .find("proprietario.id = ?1 and ativo = ?2", UUID.fromString(proprietarioId.valor), IndicativoSimNao.S)
            .list()
            .map { it.toDomain() }
    }
}
