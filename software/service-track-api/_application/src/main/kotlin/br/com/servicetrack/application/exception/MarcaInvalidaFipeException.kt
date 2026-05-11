package br.com.servicetrack.application.exception

class MarcaInvalidaFipeException(marca: String) : RuntimeException(
    "A marca '$marca' não foi encontrada na tabela FIPE. Utilize uma marca válida."
)
