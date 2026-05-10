package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.FinalizarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.shared.exception.DomainException

class FinalizarOrdemServicoService(
    private val osRepository: OrdemServicoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort,
) : FinalizarOrdemServicoUseCase {

    @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun finalizarOrdemServico(ordemServicoId: String): ResumoOrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val solicitante = usuarioRepository.buscarPorId(solicitanteId)
            ?: throw EntidadeNaoEncontradaException("Usuário", arrayOf(solicitanteId.valor))

        if (solicitante.ehCliente()) {
            throw OperacaoNegadaException(
                "finalização de OS",
                "Apenas mecânicos podem finalizar uma OS"
            )
        }

        val os = osRepository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        AuditoriaContextoHolder.registrarAntes(os)

        val servicosPendentes = os.listarServicos().filter { !it.feito }
        if (servicosPendentes.isNotEmpty()) {
            throw DomainException(
                "Não é possível finalizar a OS: ${servicosPendentes.size} serviço(s) ainda não foram concluídos"
            )
        }

        os.finalizar()

        return ResumoOrdemServicoResDTO.de(osRepository.atualizar(os))
    }
}
