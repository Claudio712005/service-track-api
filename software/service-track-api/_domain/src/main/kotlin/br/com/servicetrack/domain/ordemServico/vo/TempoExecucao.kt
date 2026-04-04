package br.com.servicetrack.domain.ordemServico.vo

import java.time.Duration
import java.time.LocalDateTime

@JvmInline
value class TempoExecucao (val valor: LocalDateTime) {

    fun calcularTempoExecucaoHoras(inicio: LocalDateTime): Long {
        return Duration.between(inicio, valor).toHours()
    }
}