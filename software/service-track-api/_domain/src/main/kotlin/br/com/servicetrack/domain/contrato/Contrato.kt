package br.com.servicetrack.domain.contrato

import br.com.servicetrack.domain.contrato.vo.ContratoId
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import java.time.LocalDateTime

class Contrato private constructor(
    val id: ContratoId,
    var aprovado: Boolean = false,
    val dataCriacao: LocalDateTime,
    var dataAtualizacao: LocalDateTime,
    val custoMaoDeObra: ValorMonetario,
    val custoInsumos: ValorMonetario,
    var observacao: String = ""
) {

    fun aprovarOrcamento(){
        aprovado = true
        dataAtualizacao = LocalDateTime.now()
        observacao += "\nOrçamento aprovado em ${dataAtualizacao.toString()};"
    }

    fun reprovarOrcamento(
        motivo: String
    ) {
        if(motivo.isBlank()){
            throw DomainException("Motivo para reprovação do orçamento deve ser informado")
        }
        aprovado = false
        dataAtualizacao = LocalDateTime.now()

        observacao += "\nMotivo da reprovação: $motivo;"
    }

}