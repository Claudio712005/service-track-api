package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.EnviarParaDiagnosticoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import jakarta.enterprise.event.Event

class EnviarParaDiagnosticoService(
    private val repository: OrdemServicoRepositoryPort,
    private val jwt: JwtPort,
    private val statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
) : EnviarParaDiagnosticoUseCase {

    @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun enviarParaDiagnostico(ordemServicoId: String): ResumoOrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val os = repository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        AuditoriaContextoHolder.registrarAntes(os)

        if (os.obterMecanicoId() != solicitanteId) {
            throw OperacaoNegadaException(
                "envio para diagnóstico",
                "Apenas o mecânico vinculado à OS pode iniciá-la em diagnóstico"
            )
        }

        os.iniciarDiagnostico()

        val atualizada = repository.atualizar(os)

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
