package br.com.servicetrack.infrastructure.veiculo.persistence

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.veiculo.dto.patch.VeiculoPatchDTO
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.shared.enums.IndicativoSimNao
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class VeiculoRepositoryAdapter : VeiculoRepositoryPort {
    override fun salvar(veiculo: Veiculo) {
        val veiculoEntity = VeiculoEntity.de(veiculo)
        veiculoEntity.persist()
    }

    override fun existeVeiculoPorPlaca(placa: String): Boolean {
        return VeiculoEntity.count("placa", placa) > 0
    }

    override fun buscarPorId(id: VeiculoId): Veiculo? {
        return VeiculoEntity.find("id", id.valor).firstResult()?.toDomain()
    }

    override fun patchVeiculo(
        id: VeiculoId,
        patchDTO: VeiculoPatchDTO
    ): Veiculo {
        val veiculo = buscarPorId(id) ?: throw EntidadeNaoEncontradaException(
            Veiculo::class.java.name,
            parametros = arrayOf(id.valor)
        )

        patchDTO.placa?.let { veiculo.alterarPlaca(it) }
        patchDTO.ativo?.let { if (it == IndicativoSimNao.N) veiculo.desativarVeiculo() }
        patchDTO.proprietarioId?.let { veiculo.atualizarProprietario(it) }

        salvar(veiculo)

        return veiculo
    }

    override fun excluir(id: VeiculoId) {
        patchVeiculo(id, VeiculoPatchDTO(null, null, IndicativoSimNao.N))
    }
}