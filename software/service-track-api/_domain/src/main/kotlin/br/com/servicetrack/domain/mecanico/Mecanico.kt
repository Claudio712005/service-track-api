package br.com.servicetrack.domain.mecanico

import br.com.servicetrack.domain.mecanico.vo.HorasTrabalho
import br.com.servicetrack.domain.mecanico.vo.NivelMecanico
import br.com.servicetrack.domain.mecanico.vo.ValorHora
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import java.math.RoundingMode

class Mecanico private constructor(
    val usuarioId: UsuarioId,
    private var valorHora: ValorHora,
    private var nivel: NivelMecanico
) {

    companion object {

        fun criar(
            usuarioId: UsuarioId,
            valorHora: ValorHora,
            nivel: NivelMecanico
        ): Mecanico {
            return Mecanico(
                usuarioId = usuarioId,
                valorHora = valorHora,
                nivel = nivel
            )
        }
    }

    fun obterNivel(): NivelMecanico = nivel

    fun obterValorHora(): ValorHora = valorHora

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
            .setScale(2, RoundingMode.HALF_UP)
        return ValorMonetario(total)
    }
}
