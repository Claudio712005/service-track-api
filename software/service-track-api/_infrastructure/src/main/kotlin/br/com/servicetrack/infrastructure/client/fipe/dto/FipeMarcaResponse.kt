package br.com.servicetrack.infrastructure.client.fipe.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class FipeMarcaResponse(
    @JsonProperty("codigo") val codigo: String,
    @JsonProperty("nome") val nome: String
)
