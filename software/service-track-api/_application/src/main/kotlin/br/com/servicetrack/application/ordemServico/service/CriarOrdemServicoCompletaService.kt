package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.ordemServico.dto.request.CriarOrdemServicoCompletaReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.CriarOrdemServicoCompletaUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.service.support.AbridorOrdemServico
import br.com.servicetrack.application.ordemServico.service.support.AssociadorItensOrdemServico
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import jakarta.enterprise.event.Event

class CriarOrdemServicoCompletaService(
    private val osRepository: OrdemServicoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort,
    private val abridor: AbridorOrdemServico,
    private val associador: AssociadorItensOrdemServico,
    private val statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
) : CriarOrdemServicoCompletaUseCase {

    @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.CRIADO)
    override fun criarOrdemServicoCompleta(req: CriarOrdemServicoCompletaReqDTO): OrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val solicitante = usuarioRepository.buscarPorId(solicitanteId)
            ?: throw EntidadeNaoEncontradaException("Usuário", arrayOf(solicitanteId.valor))

        if (!solicitante.ehMecanico()) {
            throw OperacaoNegadaException(
                "abertura completa de ordem de serviço",
                "Apenas mecânicos podem abrir uma ordem de serviço já com serviços e insumos",
            )
        }

        val os = abridor.abrir(
            req.motivo,
            req.clienteId,
            solicitanteId,
            req.veiculoId,
            req.observacao ?: "",
        )

        os.iniciarDiagnostico()
        associador.associar(os, req.servicos, req.insumos)

        val salva = osRepository.salvar(os)

        statusAlteradoEvent.fire(
            OrdemServicoStatusAlteradoEvent(
                ordemServicoId = salva.id,
                clienteId = salva.clienteId,
                novoStatus = salva.obterStatus(),
            ),
        )

        return OrdemServicoResDTO.de(salva)
    }
}
