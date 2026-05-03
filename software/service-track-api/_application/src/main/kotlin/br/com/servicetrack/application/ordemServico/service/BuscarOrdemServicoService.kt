package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.BuscarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId

class BuscarOrdemServicoService(
    private val repository: OrdemServicoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort,
) : BuscarOrdemServicoUseCase {

    override fun buscarOrdemServico(ordemServicoId: String): OrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val solicitante = usuarioRepository.buscarPorId(solicitanteId)
            ?: throw EntidadeNaoEncontradaException("Usuário", arrayOf(solicitanteId.valor))

        val os = repository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        AuditoriaContextoHolder.registrarAntes(os)

        if (solicitante.ehCliente() && os.clienteId != solicitanteId) {
            throw OperacaoNegadaException(
                "consulta de ordem de serviço",
                "Cliente só pode consultar suas próprias ordens de serviço"
            )
        }

        return OrdemServicoResDTO.de(os)
    }
}
