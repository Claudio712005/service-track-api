package br.com.servicetrack.application.exception

class OperacaoNegadaException(
    operacao: String,
    motivo: String,
) : RuntimeException(
    "Operação '$operacao' negada: $motivo."
)
