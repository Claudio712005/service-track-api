package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.dto.request.AtualizarVeiculoReqDTO
import br.com.servicetrack.application.veiculo.dto.response.DadosveiculoResDTO
import br.com.servicetrack.application.veiculo.ports.`in`.AtualizarVeiculoUseCase
import br.com.servicetrack.application.veiculo.ports.`out`.VeiculoRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.veiculo.Veiculo
import br.com.servicetrack.domain.veiculo.vo.Placa
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

class AtualizarVeiculoService(
    private val repository: VeiculoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort
) : AtualizarVeiculoUseCase {

    @Auditavel(entidade = TipoEntidade.VEICULO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun atualizarVeiculo(id: VeiculoId, req: AtualizarVeiculoReqDTO): DadosveiculoResDTO {
        val usuarioId = jwt.getUsuarioId()
        val usuario = usuarioRepository.buscarPorId(usuarioId)
            ?: throw EntidadeNaoEncontradaException(Usuario::class.java.name, arrayOf(usuarioId.valor))

        val veiculo = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Veiculo::class.java.name, arrayOf(id.valor))

        if (!veiculo.pertenceAoUsuario(usuarioId) && !usuario.ehMecanico()) {
            throw OperacaoNegadaException(
                "atualização de veículo",
                "Apenas o proprietário ou um mecânico pode atualizar este veículo"
            )
        }

        AuditoriaContextoHolder.registrarAntes(DadosveiculoResDTO.de(veiculo))

        req.placa?.let { veiculo.alterarPlaca(Placa(it)) }

        val dadosAtuais = veiculo.obterDados()
        val novoModelo = req.modelo ?: dadosAtuais.modelo
        val novaMarca  = req.marca  ?: dadosAtuais.marca
        val novoAno    = req.ano    ?: dadosAtuais.ano

        if (req.modelo != null || req.marca != null || req.ano != null) {
            veiculo.alterarDados(novoModelo, novaMarca, novoAno)
        }

        repository.atualizar(veiculo)
        return DadosveiculoResDTO.de(veiculo)
    }
}
