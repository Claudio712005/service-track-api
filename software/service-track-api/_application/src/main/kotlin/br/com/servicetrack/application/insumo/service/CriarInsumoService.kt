package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.insumo.dto.CriarInsumoReqDTO
import br.com.servicetrack.application.insumo.dto.InsumoResDTO
import br.com.servicetrack.application.insumo.ports.`in`.CriarInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.shared.vo.ValorMonetario

class CriarInsumoService(
    private val repository: InsumoRepositoryPort,
) : CriarInsumoUseCase {

    @Auditavel(entidade = TipoEntidade.INSUMO, evento = TipoEventoAuditoria.CRIADO)
    override fun criarInsumo(req: CriarInsumoReqDTO): InsumoResDTO {
        val insumo = Insumo.criar(
            nome = req.nome,
            descricao = req.descricao,
            custo = ValorMonetario(req.custo),
            qtdEstoqueInicial = req.qtdEstoqueInicial,
            estoqueMinimo = req.estoqueMinimo
        )
        repository.salvar(insumo)
        return InsumoResDTO.de(insumo)
    }
}
