package br.com.servicetrack.domain.insumo

import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import java.time.LocalDateTime

class Insumo private constructor(
    val id: InsumoId,
    val nome: String,
    val descricao: String,
    val custo: ValorMonetario,
    val dataCriacao: LocalDateTime,
    var dataAtualizacao: LocalDateTime,
) {
}