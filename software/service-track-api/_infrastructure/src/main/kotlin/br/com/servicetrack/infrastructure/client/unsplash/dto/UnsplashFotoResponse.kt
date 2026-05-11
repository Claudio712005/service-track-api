package br.com.servicetrack.infrastructure.client.unsplash.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UnsplashFotoResponse(
    @JsonProperty("urls") val urls: UnsplashUrlsResponse
)

data class UnsplashUrlsResponse(
    @JsonProperty("regular") val regular: String,
    @JsonProperty("small") val small: String,
    @JsonProperty("full") val full: String
)
