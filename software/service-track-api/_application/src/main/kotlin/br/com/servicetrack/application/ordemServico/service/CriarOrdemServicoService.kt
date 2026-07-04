package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.dto.request.OrdemServicoReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.CriarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.ordemServico.service.support.AbridorOrdemServico
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria

class CriarOrdemServicoService(
    private val repository: OrdemServicoRepositoryPort,
    private val usuarioRepositoryPort: UsuarioRepositoryPort,
    private val jwt: JwtPort,
    private val abridor: AbridorOrdemServico,
) : CriarOrdemServicoUseCase {

    @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.CRIADO)
    override fun criarOrdemServico(req: OrdemServicoReqDTO): ResumoOrdemServicoResDTO {
        val jwtIdToken = jwt.getUsuarioId()

        val usuarioLogado = usuarioRepositoryPort.buscarPorId(jwtIdToken)
            ?: throw EntidadeNaoEncontradaException("Usuário", arrayOf(jwtIdToken.valor))

        if (usuarioLogado.ehCliente() && req.clienteId != usuarioLogado.id) {
            throw OperacaoNegadaException(
                "criação de ordem de serviço",
                "Usuário logado é cliente, mas tentou criar uma ordem de serviço para outro cliente",
            )
        }

        val ordemServico = abridor.abrir(
            req.motivo,
            req.clienteId,
            req.mecanicoId,
            req.veiculoId,
            req.observaco ?: "",
        )

        return ResumoOrdemServicoResDTO.de(repository.salvar(ordemServico))
    }
}
