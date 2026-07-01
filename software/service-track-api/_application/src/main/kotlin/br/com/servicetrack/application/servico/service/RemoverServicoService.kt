package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.application.servico.ports.`in`.RemoverServicoUseCase
import br.com.servicetrack.application.servico.ports.`out`.ServicoRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId

class RemoverServicoService(
    private val repository: ServicoRepositoryPort
) : RemoverServicoUseCase {

    @Auditavel(entidade = TipoEntidade.SERVICO, evento = TipoEventoAuditoria.REMOVIDO)
    override fun removerServico(id: ServicoId) {
        val existente = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Servico::class.java.name, arrayOf(id.valor))

        repository.desativar(id)
    }
}
