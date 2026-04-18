package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.servico.dto.AtualizarServicoReqDTO
import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.application.servico.ports.`in`.AtualizarServicoUseCase
import br.com.servicetrack.application.servico.ports.`out`.ServicoRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario

class AtualizarServicoService(
    private val repository: ServicoRepositoryPort
) : AtualizarServicoUseCase {

    @Auditavel(entidade = TipoEntidade.SERVICO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun atualizarServico(id: ServicoId, req: AtualizarServicoReqDTO): ServicoResDTO {
        val existente = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Servico::class.java.name, arrayOf(id.valor))

        AuditoriaContextoHolder.registrarAntes(ServicoResDTO.de(existente))

        val atualizado = Servico.reconstituir(
            id = existente.id,
            nomeServico = req.nomeServico ?: existente.nomeServico,
            descricaoServico = req.descricaoServico ?: existente.descricaoServico,
            valorReferencia = req.valorReferencia?.let { ValorMonetario(it) } ?: existente.valorReferencia,
            dataCriacao = existente.dataCriacao,
            dataAtualizacao = existente.dataCriacao
        )

        repository.atualizar(atualizado)
        return ServicoResDTO.de(atualizado)
    }
}
