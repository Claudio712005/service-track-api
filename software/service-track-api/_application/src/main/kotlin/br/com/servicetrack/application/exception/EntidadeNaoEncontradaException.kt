package br.com.servicetrack.application.exception

class EntidadeNaoEncontradaException(
    entidade: String,
    parametros: Array<String>
) : RuntimeException(
    "Entidade '$entidade' não encontrada para os parâmetros: ${parametros.joinToString(", ")}."
)