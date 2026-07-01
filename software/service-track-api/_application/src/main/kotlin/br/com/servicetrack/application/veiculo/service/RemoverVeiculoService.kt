package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.ports.`in`.RemoverVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.`out`.VeiculoRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

class RemoverVeiculoService(
    private val jwt: JwtPort,
    private val repository: VeiculoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
): RemoverVeiculoUseCase {

    @Auditavel(evento = TipoEventoAuditoria.REMOVIDO, entidade = TipoEntidade.VEICULO)
    override fun  removerVeiculo(veiculo: VeiculoId) {
        val usuarioId = jwt.getUsuarioId()
        val usuario = usuarioRepository.buscarPorId(usuarioId) ?: throw OperacaoNegadaException("remoção de veículo", "Usuário não encontrado para o token fornecido")

        val dadosVeiculo = repository.buscarPorId(veiculo)?.obterDados() ?: throw EntidadeNaoEncontradaException(Veiculo::class.java.name, arrayOf(veiculo.valor))

        if (dadosVeiculo.proprietarioId != usuarioId && !usuario.ehMecanico()){
            throw OperacaoNegadaException("remoção de veículo", "Usuário não é o proprietário do veículo e nem mecânico, portanto não possui permissão para realizar a operação")
        }

        repository.desativar(veiculo)
    }

}
