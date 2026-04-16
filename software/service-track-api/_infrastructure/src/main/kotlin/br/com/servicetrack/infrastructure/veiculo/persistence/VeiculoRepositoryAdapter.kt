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

    override fun existeVeiculoPorPlaca(placa: String): Boolean {
        return VeiculoEntity.count("placa", placa) > 0
    }

    override fun buscarPorId(id: VeiculoId): Veiculo? {
        return VeiculoEntity
            .find("id", UUID.fromString(id.valor))
            .firstResult()
            ?.toDomain()
    }

    override fun desativar(id: VeiculoId) {
        val entity = VeiculoEntity
            .find("id", UUID.fromString(id.valor))
            .firstResult()
            ?: throw EntidadeNaoEncontradaException(
                Veiculo::class.java.name,
                parametros = arrayOf(id.valor)
            )

        entity.ativo = IndicativoSimNao.N
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
        return VeiculoEntity.listAll().map { it.toDomain() }
    }

    override fun listarPorProprietario(proprietarioId: UsuarioId): List<Veiculo> {
        return VeiculoEntity
            .find("proprietario.id", UUID.fromString(proprietarioId.valor))
            .list()
            .map { it.toDomain() }
    }
}
