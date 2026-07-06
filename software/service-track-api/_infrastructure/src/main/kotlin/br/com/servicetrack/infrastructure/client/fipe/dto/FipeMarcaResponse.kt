package br.com.servicetrack.infrastructure.client.fipe.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class FipeMarcaResponse(
    @JsonProperty("code") val codigo: String,
    @JsonProperty("name") val nome: String
)
