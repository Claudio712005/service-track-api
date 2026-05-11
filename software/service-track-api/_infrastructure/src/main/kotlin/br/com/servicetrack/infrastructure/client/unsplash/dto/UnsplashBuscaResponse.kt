package br.com.servicetrack.infrastructure.client.unsplash.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UnsplashBuscaResponse(
    @JsonProperty("results") val resultados: List<UnsplashFotoResponse>,
    @JsonProperty("total") val total: Int,
    @JsonProperty("total_pages") val totalPaginas: Int
)
