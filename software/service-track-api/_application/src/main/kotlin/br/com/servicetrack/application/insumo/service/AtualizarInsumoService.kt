package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.insumo.dto.AtualizarInsumoReqDTO
import br.com.servicetrack.application.insumo.dto.InsumoResDTO
import br.com.servicetrack.application.insumo.ports.`in`.AtualizarInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario

class AtualizarInsumoService(
    private val repository: InsumoRepositoryPort
) : AtualizarInsumoUseCase {

    @Auditavel(entidade = TipoEntidade.INSUMO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun atualizarInsumo(id: InsumoId, req: AtualizarInsumoReqDTO): InsumoResDTO {
        val existente = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Insumo::class.java.name, arrayOf(id.valor))

        AuditoriaContextoHolder.registrarAntes(InsumoResDTO.de(existente))

        val atualizado = Insumo.reconstituir(
            id = existente.id,
            nome = req.nome ?: existente.nome,
            descricao = req.descricao ?: existente.descricao,
            custo = req.custo?.let { ValorMonetario(it) } ?: existente.custo,
            estoqueMinimo = req.estoqueMinimo ?: existente.estoqueMinimo,
            qtdEstoque = existente.obterQtdEstoque(),
            dataCriacao = existente.dataCriacao,
            dataAtualizacao = existente.dataCriacao
        )

        repository.atualizar(atualizado)
        return InsumoResDTO.de(atualizado)
    }
}
