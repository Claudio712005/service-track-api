package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.insumo.dto.InsumoResDTO
import br.com.servicetrack.application.insumo.ports.`in`.RemoverInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId

class RemoverInsumoService(
    private val repository: InsumoRepositoryPort
) : RemoverInsumoUseCase {

    @Auditavel(entidade = TipoEntidade.INSUMO, evento = TipoEventoAuditoria.REMOVIDO)
    override fun removerInsumo(id: InsumoId) {
        val existente = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Insumo::class.java.name, arrayOf(id.valor))

        AuditoriaContextoHolder.registrarAntes(InsumoResDTO.de(existente))
        repository.remover(id)
    }
}
