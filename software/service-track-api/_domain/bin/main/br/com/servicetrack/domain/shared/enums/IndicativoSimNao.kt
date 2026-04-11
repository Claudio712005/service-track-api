package br.com.servicetrack.domain.shared.enums

enum class IndicativoSimNao(
    val codigo: Int,
    val descricao: String
) {
    S(1, "Sim"),
    N(0, "Não");

    companion object {
        fun fromCodigo(codigo: Int): IndicativoSimNao {
            return values().find { it.codigo == codigo }
                ?: throw IllegalArgumentException("Código inválido para IndicativoSimNao: $codigo")
        }
    }
}