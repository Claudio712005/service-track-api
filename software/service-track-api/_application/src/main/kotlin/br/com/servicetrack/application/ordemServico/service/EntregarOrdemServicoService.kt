package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.EntregarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import jakarta.enterprise.event.Event

class EntregarOrdemServicoService(
    private val osRepository: OrdemServicoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort,
    private val statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
) : EntregarOrdemServicoUseCase {

    @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun entregarOrdemServico(ordemServicoId: String): ResumoOrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val solicitante = usuarioRepository.buscarPorId(solicitanteId)
            ?: throw EntidadeNaoEncontradaException("Usuário", arrayOf(solicitanteId.valor))

        if (solicitante.ehCliente()) {
            throw OperacaoNegadaException(
                "entrega de OS",
                "Apenas mecânicos podem registrar a entrega de uma OS"
            )
        }

        val os = osRepository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        os.entregar()

        val atualizada = osRepository.atualizar(os)

        statusAlteradoEvent.fire(
            OrdemServicoStatusAlteradoEvent(
                ordemServicoId = atualizada.id,
                clienteId = atualizada.clienteId,
                novoStatus = atualizada.obterStatus(),
            )
        )

        return ResumoOrdemServicoResDTO.de(atualizada)
    }
}
