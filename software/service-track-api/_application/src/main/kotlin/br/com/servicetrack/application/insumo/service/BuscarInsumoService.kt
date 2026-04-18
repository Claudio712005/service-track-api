package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.insumo.dto.InsumoResDTO
import br.com.servicetrack.application.insumo.ports.`in`.BuscarInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId

class BuscarInsumoService(
    private val repository: InsumoRepositoryPort
) : BuscarInsumoUseCase {

    override fun buscarInsumo(id: InsumoId): InsumoResDTO {
        val insumo = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Insumo::class.java.name, arrayOf(id.valor))
        return InsumoResDTO.de(insumo)
    }
}
