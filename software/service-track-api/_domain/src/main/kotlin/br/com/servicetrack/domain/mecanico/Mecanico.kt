package br.com.servicetrack.domain.mecanico

import br.com.servicetrack.domain.mecanico.vo.HorasTrabalho
import br.com.servicetrack.domain.mecanico.vo.NivelMecanico
import br.com.servicetrack.domain.mecanico.vo.ValorHora
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import br.com.servicetrack.domain.usuario.vo.UsuarioId

class Mecanico private constructor(
    val usuarioId: UsuarioId,
    private var valorHora: ValorHora,
    private var nivel: NivelMecanico
) {
    fun promover(): Mecanico {
        return Mecanico(
            usuarioId = this.usuarioId,
            valorHora = this.valorHora,
            nivel = NivelMecanico.criar(this.nivel.proximoNivel())
        )
    }

    fun calcularCusto(horas: HorasTrabalho): ValorMonetario {
        val total = valorHora.valor
            .multiply(horas.valor.toBigDecimal())
            .multiply(nivel.multiplicador().toBigDecimal())

        return ValorMonetario(total)
    }
}