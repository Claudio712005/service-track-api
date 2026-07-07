package br.com.servicetrack.application.ordemServico.service.support

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

class AbridorOrdemServico(
    private val usuarioRepository: UsuarioRepositoryPort,
    private val osRepository: OrdemServicoRepositoryPort,
) {

    fun abrir(
        motivo: String,
        clienteId: UsuarioId,
        mecanicoId: UsuarioId,
        veiculoId: VeiculoId,
        observacao: String,
    ): OrdemServico {
        val cliente = usuarioRepository.buscarPorId(clienteId)
            ?: throw EntidadeNaoEncontradaException("Cliente", arrayOf(clienteId.valor))

        if (cliente.ehMecanico()) {
            throw OperacaoNegadaException(
                "criação de ordem de serviço",
                "Usuário informado como cliente é um mecânico, e não um cliente",
            )
        }

        val mecanico = usuarioRepository.buscarPorId(mecanicoId)
            ?: throw EntidadeNaoEncontradaException("Mecânico", arrayOf(mecanicoId.valor))

        if (mecanico.ehCliente()) {
            throw OperacaoNegadaException(
                "criação de ordem de serviço",
                "Usuário informado como mecânico é um cliente, e não um mecânico",
            )
        }

        val ossAbertas = osRepository.contarOsAbertaPorIdVeiculoEIdCliente(clienteId, veiculoId)
        if (ossAbertas > 0L) {
            throw OperacaoNegadaException(
                "criação de ordem de serviço",
                "Já existe uma ordem de serviço aberta para o veículo informado e cliente informado",
            )
        }

        return OrdemServico.abrir(motivo, clienteId, mecanicoId, veiculoId, observacao)
    }
}
