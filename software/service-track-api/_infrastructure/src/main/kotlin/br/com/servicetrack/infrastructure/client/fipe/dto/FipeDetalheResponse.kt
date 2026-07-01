package br.com.servicetrack.infrastructure.client.fipe.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class FipeDetalheResponse(
    @JsonProperty("codeFipe") val codigoFipe: String,
    @JsonProperty("brand") val marca: String,
    @JsonProperty("model") val modelo: String,
    @JsonProperty("modelYear") val anoModelo: Int,
    @JsonProperty("fuel") val combustivel: String,
    @JsonProperty("price") val valor: String,
    @JsonProperty("referenceMonth") val mesReferencia: String
)
