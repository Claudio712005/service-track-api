package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.dto.request.OrdemServicoReqDTO
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.CriarOrdemServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.ordemServico.OrdemServico

class CriarOrdemServicoService(
    private val repository: OrdemServicoRepositoryPort,
    private val usuarioRepositoryPort: UsuarioRepositoryPort,
    private val jwt: JwtPort
): CriarOrdemServicoUseCase {

    @Auditavel(entidade = TipoEntidade.ORDEM_SERVICO, evento = TipoEventoAuditoria.CRIADO)
    override fun criarOrdemServico(req: OrdemServicoReqDTO): ResumoOrdemServicoResDTO {

        val jwtIdToken = jwt.getUsuarioId()

        val usuarioLogado = usuarioRepositoryPort.buscarPorId(
            jwtIdToken
        ) ?: throw EntidadeNaoEncontradaException("Usuário", arrayOf(jwtIdToken.valor))

        if(usuarioLogado.ehCliente() && req.clienteId != usuarioLogado.id) {
            throw OperacaoNegadaException(
                "criação de ordem de serviço",
                "Usuário logado é cliente, mas tentou criar uma ordem de serviço para outro cliente"
            )
        }

        val cliente = usuarioRepositoryPort.buscarPorId(
            req.clienteId
        ) ?: throw EntidadeNaoEncontradaException("Cliente", arrayOf(req.clienteId.valor))

        if(cliente.ehMecanico()){
            throw OperacaoNegadaException(
                "criação de ordem de serviço",
                "Usuário informado como cliente é um mecânico, e não um cliente"
            )
        }

        val mecanico = usuarioRepositoryPort.buscarPorId(
            req.mecanicoId
        ) ?: throw EntidadeNaoEncontradaException("Mecânico", arrayOf(req.mecanicoId.valor))

        if(mecanico.ehCliente()){
            throw OperacaoNegadaException(
                "criação de ordem de serviço",
                "Usuário informado como mecânico é um cliente, e não um mecânico"
            )
        }

        val ossAbertas = repository.contarOsAbertaPorIdVeiculoEIdCliente(req.clienteId, req.veiculoId)
        if(ossAbertas > 0L) {
            throw OperacaoNegadaException(
                "criação de ordem de serviço",
                "Já existe uma ordem de serviço aberta para o veículo informado e cliente informado"
            )
        }

        val ordemServico = OrdemServico.abrir(
            req.motivo,
            req.clienteId,
            req.mecanicoId,
            req.veiculoId,
            req.observaco ?: ""
        )

        return ResumoOrdemServicoResDTO.de(repository.salvar(ordemServico))
    }

}