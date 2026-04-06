package br.com.servicetrack.infrastructure.veiculo.persistence

import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.veiculo.Veiculo
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class VeiculoRepositoryAdapter: VeiculoRepositoryPort {
    override fun salvar(veiculo: Veiculo) {
        val veiculoEntity = VeiculoEntity.de(veiculo)
        veiculoEntity.persist()
    }

    override fun existeVeiculoPorPlaca(placa: String): Boolean {
        return VeiculoEntity.count("placa", placa) > 0
    }
}