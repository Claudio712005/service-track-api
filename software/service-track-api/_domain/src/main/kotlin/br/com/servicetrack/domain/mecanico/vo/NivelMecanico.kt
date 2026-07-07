package br.com.servicetrack.domain.mecanico.vo

import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class NivelMecanico private constructor(
    val valor: NivelMecanicoEnum
) {

    companion object {
        fun criar(valor: NivelMecanicoEnum): NivelMecanico {
            return NivelMecanico(valor)
        }
    }

    fun proximoNivel(): NivelMecanicoEnum {
        return when (valor) {
            NivelMecanicoEnum.JUNIOR -> NivelMecanicoEnum.PLENO
            NivelMecanicoEnum.PLENO -> NivelMecanicoEnum.SENIOR
            NivelMecanicoEnum.SENIOR -> throw DomainException("O nível SENIOR não pode ser promovido")
        }
    }

    fun multiplicador(): Int {
        return when (valor) {
            NivelMecanicoEnum.JUNIOR -> 1
            NivelMecanicoEnum.PLENO -> 2
            NivelMecanicoEnum.SENIOR -> 3
        }
    }
}
