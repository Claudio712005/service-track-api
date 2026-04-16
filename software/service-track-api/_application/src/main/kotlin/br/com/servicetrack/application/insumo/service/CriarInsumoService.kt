package br.com.servicetrack.application.insumo.service

import br.com.servicetrack.application.insumo.dto.CriarInsumoReqDTO
import br.com.servicetrack.application.insumo.dto.InsumoResDTO
import br.com.servicetrack.application.insumo.ports.`in`.CriarInsumoUseCase
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.domain.insumo.Insumo
import br.com.servicetrack.domain.shared.vo.ValorMonetario

class CriarInsumoService(
    private val repository: InsumoRepositoryPort
) : CriarInsumoUseCase {

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
