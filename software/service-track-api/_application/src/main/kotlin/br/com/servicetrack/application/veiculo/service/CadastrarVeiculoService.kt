package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.exception.VeiculoJaExisteException
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.dto.request.CadastrarVeiculoReqDTO
import br.com.servicetrack.application.veiculo.dto.response.DadosveiculoResDTO
import br.com.servicetrack.application.veiculo.mapper.toDomain
import br.com.servicetrack.application.veiculo.ports.`in`.CadastrarVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.`out`.VeiculoRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.UsuarioId

class CadastrarVeiculoService(
    private val repository: VeiculoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort
): CadastrarVeiculoUseCase {

    @Auditavel(entidade = TipoEntidade.VEICULO, evento = TipoEventoAuditoria.CRIADO)
    override fun cadastrarVeiculo(req: CadastrarVeiculoReqDTO): DadosveiculoResDTO {

        val veiculoInativo = repository.buscarInativoPorPlaca(req.placa)
        if (veiculoInativo != null) {
            return reativarVeiculo(veiculoInativo.obterDados().id, req)
        }

        if (repository.existeVeiculoPorPlaca(req.placa)) {
            throw VeiculoJaExisteException(req.placa)
        }

        val proprietario = usuarioRepository.buscarPorId(UsuarioId(req.proprietarioId))
            ?: throw EntidadeNaoEncontradaException(Usuario.Companion::class.java.toString(), arrayOf(req.proprietarioId))

        val usuarioIdToken = jwt.getUsuarioId()

        val usuarioToken = usuarioRepository.buscarPorId(usuarioIdToken)
            ?: throw EntidadeNaoEncontradaException(Usuario.Companion::class.java.toString(), arrayOf(usuarioIdToken.valor))

        if (usuarioToken.id != proprietario.id) {
            if (!usuarioToken.ehMecanico()) {
                throw OperacaoNegadaException("cadastro de veículo", "Um cliente não pode cadastrar um veículo para outro cliente, apenas um mecânico pode realizar esse tipo de operação")
            }
        }

        val veiculo = req.toDomain()
        repository.salvar(veiculo)

        return DadosveiculoResDTO.de(veiculo)
    }

    private fun reativarVeiculo(id: br.com.servicetrack.domain.veiculo.vo.VeiculoId, req: CadastrarVeiculoReqDTO): DadosveiculoResDTO {
        repository.reativar(id)
        val reativado = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(br.com.servicetrack.domain.veiculo.Veiculo::class.java.name, arrayOf(id.valor))
        reativado.alterarDados(req.modelo, req.marca, req.ano)
        repository.atualizar(reativado)
        return DadosveiculoResDTO.de(reativado)
    }

}