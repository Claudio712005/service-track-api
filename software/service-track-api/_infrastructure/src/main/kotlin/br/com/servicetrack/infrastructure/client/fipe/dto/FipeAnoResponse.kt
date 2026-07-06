package br.com.servicetrack.infrastructure.client.fipe.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class FipeAnoResponse(
    @JsonProperty("code") val codigo: String,
    @JsonProperty("name") val nome: String
)
